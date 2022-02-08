package _kaudio.reverb

class CombFilter(size: Int) {
    private val buffer = RingBuffer(size)
    private var tmp = 0f
    var feedback = 0f
    private var damp = 0f
    private var dampInv = 1f

    fun setDamp(value: Float) {
        damp = value
        dampInv = 1f - damp
    }

    fun mute() {
        tmp = 0f
        buffer.fill(0f)
    }

    fun process(sample: Float): Float {
        val output = buffer.get()
        tmp = output * dampInv + tmp * damp
        buffer.put(sample + tmp * feedback)
        return output
    }
}
