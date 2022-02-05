package _kaudio.nodes.util

import _kaudio.FRAME_SIZE
import _kaudio.nodes.abstract.MonoNode
import _kaudio.nodes.abstract.PyType_MonoNode
import _kaudio.nodes.abstract.PyType_StereoNode
import _kaudio.nodes.abstract.StereoNode
import _kaudio.utils.copyStereo
import kotlinx.cinterop.*
import python.*
import pywrapper.PyObjectT
import pywrapper.builders.makePyType
import pywrapper.ext.arg
import pywrapper.ext.cast
import pywrapper.ext.kt
import pywrapper.ext.parseKw

class StereoSync(private val left: MonoNode, private val right: MonoNode) : StereoNode() {
    private val dummy = DummyNode(true)

    init {
        left.connect("output", dummy, "input_left")
        right.connect("output", dummy, "input_right")
    }

    override fun process() {
        copyStereo(inputLeft, inputRight, left.input, right.input)

        left.process()
        right.process()

        copyStereo(dummy.inputLeft, dummy.inputRight, outputLeft, outputRight)
    }
}

val initStereoSync = staticCFunction { self: PyObjectT, args: PyObjectT, kwargs: PyObjectT ->
    memScoped {
        val selfObj: CPointer<KtPyObject> = self?.reinterpret() ?: return@memScoped -1

        val parsed = args.parseKw("__init__", kwargs, "node_left", "node_right")
        if (parsed.isEmpty()) {
            return@memScoped -1
        }

        val leftPy = parsed.arg<CPointer<PyObject>>("node_left")
        val rightPy = parsed.arg<CPointer<PyObject>>("node_right")

        if (PyObject_IsInstance(leftPy, PyType_MonoNode.ptr.reinterpret()) != 1) {
            PyErr_SetString(PyExc_TypeError, "Expected parameter 0 to be a MonoNode")
            return@memScoped -1
        }

        if (PyObject_IsInstance(rightPy, PyType_MonoNode.ptr.reinterpret()) != 1) {
            PyErr_SetString(PyExc_TypeError, "Expected parameter 1 to be a MonoNode")
            return@memScoped -1
        }

        val instance = StereoSync(leftPy.kt.cast(), rightPy.kt.cast())
        val ref = StableRef.create(instance)
        selfObj.pointed.ktObject = ref.asCPointer()
        return@memScoped 0
    }
}

val PyType_StereoSync = makePyType<StereoSync>(
    ktp_base = PyType_StereoNode.ptr,
    ktp_init = initStereoSync
)