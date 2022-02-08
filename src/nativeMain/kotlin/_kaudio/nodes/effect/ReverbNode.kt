package _kaudio.nodes.effect

import _kaudio.FRAME_SIZE
import _kaudio.nodes.abstract.PyType_StereoNode
import _kaudio.nodes.abstract.StereoNode
import kotlinx.cinterop.*
import python.KtPyObject
import pywrapper.PyObjectT
import pywrapper.builders.makePyType
import freeverb.*
import pywrapper.NeedsFree


class ReverbNode : StereoNode(), NeedsFree {
    private val model = create_revmodel();

    private val roomSize by attribute("room_size", getroomsize(model)) {
        setroomsize(model, it)
    }
    private val damp by attribute("damp", getdamp(model)) {
        setdamp(model, it)
    }
    private val wet by attribute("wet", getwet(model)) {
        setwet(model, it)
    }
    private val dry by attribute("dry", getdry(model)) {
        setdry(model, it)
    }
    private val width by attribute("width", getwidth(model)) {
        setwidth(model, it)
    }

    private val inLeft = nativeHeap.allocArray<FloatVar>(FRAME_SIZE)
    private val inRight = nativeHeap.allocArray<FloatVar>(FRAME_SIZE)
    private val outLeft = nativeHeap.allocArray<FloatVar>(FRAME_SIZE)
    private val outRight = nativeHeap.allocArray<FloatVar>(FRAME_SIZE)

    override fun free() {
        delete_revmodel(model)
        nativeHeap.free(inLeft)
        nativeHeap.free(inRight)
        nativeHeap.free(outLeft)
        nativeHeap.free(outRight)
    }

    override fun process() {
        val inL = inputLeft
        val inR = inputRight
        val outL = outputLeft
        val outR = outputRight

        for (i in 0 until FRAME_SIZE) {
            inLeft[i] = inL[i]
            inRight[i] = inR[i]
        }

        processreplace(model, inLeft, inRight, outLeft, outRight, FRAME_SIZE.convert(), 1)

        for (i in 0 until FRAME_SIZE) {
            outL[i] = outLeft[i]
            outR[i] = outRight[i]
        }
    }
}

private val initReverbNode = staticCFunction { self: PyObjectT, args: PyObjectT, kwargs: PyObjectT ->
    memScoped {
        val selfObj: CPointer<KtPyObject> = self?.reinterpret() ?: return@memScoped -1

        val instance = ReverbNode()
        val ref = StableRef.create(instance)
        selfObj.pointed.ktObject = ref.asCPointer()
        return@memScoped 0
    }
}

val PyType_ReverbNode = makePyType<ReverbNode>(
    ktp_base = PyType_StereoNode.reinterpret(),
    ktp_init = initReverbNode,
)
