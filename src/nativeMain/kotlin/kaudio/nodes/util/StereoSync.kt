package kaudio.nodes.util

import kaudio.FRAME_SIZE
import kaudio.nodes.abstract.MonoNode
import kaudio.nodes.abstract.PyType_MonoNode
import kaudio.nodes.abstract.PyType_StereoNode
import kaudio.nodes.abstract.StereoNode
import kotlinx.cinterop.*
import python.*
import pywrapper.PyObjectT
import pywrapper.builders.makePyType
import pywrapper.ext.cast
import pywrapper.ext.kt

class StereoSync(private val left: MonoNode, private val right: MonoNode) : StereoNode() {
    private val dummy = DummyNode(true)

    init {
        left.connect("output", dummy, "input_left")
        right.connect("output", dummy, "input_right")
    }

    override fun process() {
        for (i in 0 until FRAME_SIZE) {
            left.input[i] = inputLeft[i]
            right.input[i] = inputRight[i]
        }

        left.process()
        right.process()

        for (i in 0 until FRAME_SIZE) {
            outputLeft[i] = dummy.inputLeft[i]
            outputRight[i] = dummy.inputRight[i]
        }
    }
}

val initStereoSync = staticCFunction { self: PyObjectT, args: PyObjectT, kwargs: PyObjectT ->
    memScoped {
        val selfObj: CPointer<KtPyObject> = self?.reinterpret() ?: return@memScoped -1
        val leftC = allocPointerTo<PyObject>()
        val rightC = allocPointerTo<PyObject>()

        if (PyArg_ParseTuple(args, "OO", leftC.ptr, rightC.ptr) == 0) {
            PyErr_SetString(PyExc_TypeError, "Expected an int")
            return@memScoped -1
        }

        if (PyObject_IsInstance(leftC.value, PyType_MonoNode.ptr.reinterpret()) != 1) {
            PyErr_SetString(PyExc_TypeError, "Expected parameter 0 to be a MonoNode")
            return@memScoped -1
        }

        if (PyObject_IsInstance(rightC.value, PyType_MonoNode.ptr.reinterpret()) != 1) {
            PyErr_SetString(PyExc_TypeError, "Expected parameter 1 to be a MonoNode")
            return@memScoped -1
        }

        val instance = StereoSync(leftC.value!!.kt.cast(), rightC.value!!.kt.cast())
        val ref = StableRef.create(instance)
        selfObj.pointed.ktObject = ref.asCPointer()
        return@memScoped 0
    }
}

val PyType_StereoSync = makePyType<StereoSync>(
    ktp_base = PyType_StereoNode.ptr,
    ktp_init = initStereoSync
)