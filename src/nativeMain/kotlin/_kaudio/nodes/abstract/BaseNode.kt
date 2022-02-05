package _kaudio.nodes.abstract

import _kaudio.FRAME_SIZE
import _kaudio.utils.Configurable
import _kaudio.utils.Delegate
import _kaudio.utils.PyType_Configurable
import kotlinx.cinterop.*
import python.*
import pywrapper.PyObjectT
import pywrapper.builders.makePyType
import pywrapper.ext.*
import pywrapper.ext.cast
import pywrapper.ext.incref
import pywrapper.ext.kt
import pywrapper.ext.pydef

val DUMMY_ARRAY = FloatArray(FRAME_SIZE) { 0f }

abstract class BaseNode : Configurable() {
    fun input(name: String): Delegate<FloatArray> {
        val arr = FloatArray(FRAME_SIZE) { 0f }
        inputs[name] = arr
        return Delegate { arr }
    }

    fun output(name: String): Delegate<FloatArray> {
        outputs[name] = Delegate {
            DUMMY_ARRAY
        }

        return Delegate {
            val x by outputs[name]!!
            x
        }
    }

    private val inputs = mutableMapOf<String, FloatArray>()
    private val outputs = mutableMapOf<String, Delegate<FloatArray>>()

    internal fun removeInput(name: String) {
        inputs.remove(name)
    }

    internal fun removeOutput(name: String) {
        outputs.remove(name)
    }

    internal val inputNames
        get() = inputs.keys.toList()
    internal val outputNames
        get() = outputs.keys.toList()

    internal fun clearOutput(name: String) {
        val out by outputs[name]!!
        out.fill(0f)
    }

    fun connect(output: String, node: BaseNode, input: String) {
        outputs[output] = Delegate { node.inputs[input]!! }
    }

    fun disconnect(output: String) {
        outputs[output] = Delegate {
            DUMMY_ARRAY
        }
    }

    abstract fun process()
}

private fun getPyType_BaseNode(): PyObjectT = PyType_BaseNode.ptr.reinterpret()

private val connect = staticCFunction { self: PyObjectT, args: PyObjectT, kwargs: PyObjectT ->
    memScoped {
        val selfKt = self!!.kt.cast<BaseNode>()

        val parsed = args.parseKw("connect", kwargs, "output", "node", "input")
        if (parsed.isEmpty()) {
            return@staticCFunction null
        }

        if (PyObject_IsInstance(parsed.arg("node"), getPyType_BaseNode()) != 1) {
            PyErr_SetString(PyExc_TypeError, "Expected parameter 2 to be a subclass of BaseNode")
            return@memScoped null
        }

        selfKt.connect(
            parsed.arg("output"),
            parsed.arg<CPointer<PyObject>>("node").kt.cast(),
            parsed.arg("input")
        )

        Py_None.incref()
    }
}.pydef("connect", "Connects the output to the input of another node", METH_VARARGS or METH_KEYWORDS)


private val disconnect = staticCFunction { self: PyObjectT, args: PyObjectT, kwargs: PyObjectT ->
    memScoped {
        val selfKt = self!!.kt.cast<BaseNode>()

        val parsed = args.parseKw("disconnect", kwargs, "output")
        if (parsed.isEmpty()) {
            return@staticCFunction null
        }

        val output = parsed.arg<String>("output")
        selfKt.clearOutput(output)
        selfKt.disconnect(output)

        Py_None.incref()
    }
}.pydef("disconnect", "Disconnects the output", METH_VARARGS or METH_KEYWORDS)


private val connectStereo = staticCFunction { self: PyObjectT, args: PyObjectT, kwargs: PyObjectT ->
    memScoped {
        val selfKt = self!!.kt.cast<StereoNode>()

        val parsed = args.parseKw("connect_stereo", kwargs, "node")
        if (parsed.isEmpty()) {
            return@staticCFunction null
        }

        val nodePy = parsed.arg<CPointer<PyObject>>("node")

        if (PyObject_IsInstance(nodePy, getPyType_BaseNode()) != 1) {
            PyErr_SetString(PyExc_TypeError, "Expected parameter 2 to be a subclass of BaseNode")
            return@memScoped null
        }

        val node = nodePy.kt.cast<BaseNode>()

        selfKt.connect("output_left", node, "input_left")
        selfKt.connect("output_right", node, "input_right")

        Py_None.incref()
    }
}.pydef("connect_stereo", "Connects the outputs to the inputs of another node", METH_VARARGS)


private val process = staticCFunction { self: PyObjectT, args: PyObjectT ->
    val selfKt = self!!.kt.cast<BaseNode>()
    selfKt.process()
    Py_None.incref()
}.pydef("process", "Tells the node to process")

private val getInputs = staticCFunction { self: PyObjectT, args: PyObjectT ->
    val selfKt = self!!.kt.cast<BaseNode>()
    val inputs = selfKt.inputNames.map {
        PyUnicode_FromString(it).incref()
    }.let {
        val list = PyList_New(it.size.convert())
        it.forEachIndexed { index, item ->
            PyList_SetItem(list, index.convert(), item)
        }
        list
    }

    inputs.incref()
}.pydef("inputs", "Returns a list of the node's inputs")

private val getOutputs = staticCFunction { self: PyObjectT, args: PyObjectT ->
    val selfKt = self!!.kt.cast<BaseNode>()
    val outputs = selfKt.outputNames.map {
        PyUnicode_FromString(it).incref()
    }.let {
        val list = PyList_New(it.size.convert())
        it.forEachIndexed { index, item ->
            PyList_SetItem(list, index.convert(), item)
        }
        list
    }

    outputs.incref()
}.pydef("outputs", "Returns a list of the node's outputs")

val PyType_BaseNode = makePyType<BaseNode>(
    ktp_base = PyType_Configurable.ptr,
    ktp_methods = listOf(
        connect,
        disconnect,
        connectStereo,
        process,
        getInputs,
        getOutputs,
    )
)
