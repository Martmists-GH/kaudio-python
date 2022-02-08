package _kaudio.nodes.effect

import _kaudio.FRAME_SIZE
import _kaudio.nodes.abstract.PyType_StereoNode
import _kaudio.nodes.abstract.StereoNode
import _kaudio.reverb.RevModel
import _kaudio.utils.copyStereo
import kotlinx.cinterop.*
import python.KtPyObject
import pywrapper.PyObjectT
import pywrapper.builders.makePyType

class ReverbNode : StereoNode() {
    private val model = RevModel()
    private val roomSize by attribute("room_size", 0f) {
        model.setRoomSize(it)
    }
    private val damp by attribute("damp", 0f) {
        model.setDamp(it)
    }
    private val wet by attribute("wet", 0f) {
        model.setWet(it)
    }
    private val dry by attribute("dry", 0.5f) {
        model.setDry(it)
    }
    private val width by attribute("width", 0f) {
        model.setWidth(it)
    }

    init {
        model.setRoomSize(roomSize)
        model.setWidth(width)
        model.setDamp(damp)
        model.setWet(wet)
        model.setDry(dry)
    }

    override fun process() {
        val inL = inputLeft
        val inR = inputRight

        model.processReplace(inL, inR, FRAME_SIZE)
        copyStereo(inL, inR, outputLeft, outputRight)
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
