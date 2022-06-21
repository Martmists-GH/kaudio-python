package kaudio.nodes.effect

import bs2b.*
import kaudio.FRAME_SIZE
import kaudio.SAMPLERATE
import kaudio.nodes.base.StereoNode
import kotlinx.cinterop.*
import kpy.annotations.PyExport
import kpy.utilities.Freeable

@PyExport
class Bs2bNode : StereoNode(), Freeable {
    private val config = bs2b_open()
    private val buffer = nativeHeap.allocArray<FloatVar>(2L) { this.value = 0f }

    init {
        bs2b_set_srate(config, SAMPLERATE.convert())
        bs2b_set_level(config, BS2B_DEFAULT_CLEVEL)
    }

    private val frequency by python(bs2b_get_level_fcut(config)) {
        bs2b_set_level_fcut(config, it)
    }
    private val feed by python(bs2b_get_level_feed(config)) {
        bs2b_set_level_feed(config, it)
    }

    override fun process() {
        val inL = inputLeft
        val inR = inputRight
        val outL = outputLeft
        val outR = outputRight

        for (i in 0 until FRAME_SIZE) {
            buffer[0] = inL[i]
            buffer[1] = inR[i]

            bs2b_cross_feed_f(config, buffer, 1)

            outL[i] = buffer[0]
            outR[i] = buffer[1]
        }
    }

    override fun free() {
        bs2b_close(config)
        nativeHeap.free(buffer)
    }
}
