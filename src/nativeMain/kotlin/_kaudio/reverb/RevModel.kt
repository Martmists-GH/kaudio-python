package _kaudio.reverb

class RevModel {
    private var gain = 0f
    private var roomSize = 0f
    private var roomSizeUsed = 0f
    private var damp = 0f
    private var dampUsed = 0f
    private var wet = 0f
    private var wetChannel = 0f
    private var wetCross = 0f
    private var dry = 0f
    private var width = 0f
    private var mode = 0

    private val combFiltersL = arrayListOf(
        CombFilter(1116),
        CombFilter(1188),
        CombFilter(1277),
        CombFilter(1356),
        CombFilter(1356),
        CombFilter(1491),
        CombFilter(1557),
        CombFilter(1617),
    )
    private val combFiltersR = arrayListOf(
        CombFilter(1139),
        CombFilter(1211),
        CombFilter(1300),
        CombFilter(1379),
        CombFilter(1445),
        CombFilter(1514),
        CombFilter(1580),
        CombFilter(1640),
    )
    private val allpassFiltersL = arrayListOf(
        AllpassFilter(556),
        AllpassFilter(441),
        AllpassFilter(341),
        AllpassFilter(225),
    )
    private val allpassFiltersR = arrayListOf(
        AllpassFilter(579),
        AllpassFilter(464),
        AllpassFilter(364),
        AllpassFilter(248),
    )

    init {
        for (i in 0 until 4) {
            allpassFiltersL[i].feedback = 0.5f
            allpassFiltersR[i].feedback = 0.5f
        }

        setWet(1/3f)
        setRoomSize(0.5f)
        setDamp(0f)
        setWidth(1f)
        setMode(0)
        mute()
    }

    private fun mute() {
        if (mode == 1) {
            return
        }

        for (i in 0 until 8) {
            combFiltersL[i].mute()
            combFiltersR[i].mute()
        }

        for (i in 0 until 4) {
            allpassFiltersL[i].mute()
            allpassFiltersR[i].mute()
        }
    }

    fun processReplace(bufL: FloatArray, bufR: FloatArray, size: Int) {
        for (i in 0 until size) {
            var outL = 0f
            var outR = 0f
            val input = (bufL[i] + bufR[i]) * gain

            for (j in 0 until 8) {
                outL += combFiltersL[j].process(input)
                outR += combFiltersR[j].process(input)
            }

            for (j in 0 until 4) {
                outL = allpassFiltersL[j].process(outL)
                outR = allpassFiltersR[j].process(outR)
            }

            bufL[i] = outL * wetChannel + outR * wetCross + bufL[i] * dry
            bufR[i] = outR * wetChannel + outL * wetCross + bufR[i] * dry
        }
    }

    private fun updateCoeffs() {
        wetChannel = wet * (width/2f + 0.5f)
        wetCross = (1 - width)/2f * wet

        if (mode == 1) {
            roomSizeUsed = 1f
            dampUsed = 0f
            gain = 0f
        } else {
            roomSizeUsed = roomSize
            dampUsed = damp
            gain = 0.015f
        }

        for (i in 0 until 8) {
            combFiltersL[i].feedback = roomSizeUsed
            combFiltersR[i].feedback = roomSizeUsed
        }

        for (i in 0 until 8) {
            combFiltersL[i].setDamp(dampUsed)
            combFiltersL[i].setDamp(dampUsed)
        }
    }

    fun reset() {
        for (i in 0 until 8) {
            combFiltersL[i].mute()
            combFiltersR[i].mute()
        }

        for (i in 0 until 4) {
            allpassFiltersL[i].mute()
            allpassFiltersR[i].mute()
        }
    }

    fun setRoomSize(value: Float) {
        roomSize = value * 0.28f + 0.7f
        updateCoeffs()
    }

    fun setDamp(value: Float) {
        damp = value * 0.4f
        updateCoeffs()
    }

    fun setWet(value: Float) {
        wet = value * 3f
        updateCoeffs()
    }

    fun setDry(value: Float) {
        dry = value * 2f
        updateCoeffs()
    }

    fun setWidth(value: Float) {
        width = value
        updateCoeffs()
    }

    fun setMode(value: Int) {
        mode = value
        updateCoeffs()
    }
}
