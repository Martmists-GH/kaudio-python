package _kaudio.nodes.effect

import _kaudio.FRAME_SIZE
import _kaudio.nodes.abstract.PyType_StereoNode
import _kaudio.nodes.abstract.StereoNode
import bs2b.*
import kotlinx.cinterop.*
import python.KtPyObject
import pywrapper.NeedsFree
import pywrapper.PyObjectT
import pywrapper.builders.makePyType

class Bs2bNode : StereoNode(), NeedsFree {
    private val config = bs2b_open()
    private val buffer = nativeHeap.allocArray<FloatVar>(2L) { this.value = 0f }

    init {
        bs2b_set_srate(config, 48000.convert())
        bs2b_set_level(config, BS2B_DEFAULT_CLEVEL)
    }

    private val frequency by attribute("frequency", bs2b_get_level_fcut(config)) {
        bs2b_set_level_fcut(config, it)
    }
    private val feed by attribute("feed", bs2b_get_level_feed(config)) {
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

val initBs2bNode = staticCFunction { self: PyObjectT, args: PyObjectT, kwargs: PyObjectT ->
    memScoped {
        val selfObj: CPointer<KtPyObject> = self?.reinterpret() ?: return@memScoped -1
        val instance = Bs2bNode()
        val ref = StableRef.create(instance)
        selfObj.pointed.ktObject = ref.asCPointer()
        return@memScoped 0
    }
}

val PyType_Bs2bNode = makePyType<Bs2bNode>(
    ktp_init = initBs2bNode,
    ktp_base = PyType_StereoNode.ptr,
)
