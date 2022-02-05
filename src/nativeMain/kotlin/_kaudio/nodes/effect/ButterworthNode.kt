package _kaudio.nodes.effect

import _kaudio.utils.BiquadType
import _kaudio.utils.makeBiquad
import kotlinx.cinterop.*
import python.*
import pywrapper.PyObjectT
import pywrapper.builders.makePyType
import pywrapper.ext.*
import pywrapper.ext.kt

class ButterworthNode(stereo: Boolean) : IIRNode(2, stereo) {
    fun make(type: BiquadType, fc: Int, gain: Float) {
        makeBiquad(type, stereo, fc, gain, this)
    }
}

private val initButterworthNode = staticCFunction { self: PyObjectT, args: PyObjectT, kwargs: PyObjectT ->
    memScoped {
        val selfObj: CPointer<KtPyObject> = self?.reinterpret() ?: return@memScoped -1

        val parsed = args.parseKw("__init__", kwargs, "stereo")
        if (parsed.isEmpty()) {
            return@memScoped -1
        }

        val instance = ButterworthNode(parsed.arg("stereo"))
        val ref = StableRef.create(instance)
        selfObj.pointed.ktObject = ref.asCPointer()
        return@memScoped 0
    }
}

private val makeButterworth = staticCFunction { self: PyObjectT, args: PyObjectT, kwargs: PyObjectT ->
    memScoped {
        val node = self!!.kt.cast<ButterworthNode>()
        val parsed = args.parseKw("set", kwargs, "type", "fc", "gain")
        if (parsed.isEmpty()) {
            return@memScoped null
        }

        node.make(
            BiquadType.values().first { it.name.lowercase() == parsed.arg<String>("type").lowercase() },
            parsed.arg("fc"),
            parsed.arg("gain")
        )
    }

    Py_None.incref()
}.pydef("make_butterworth", "Create 2nd-order Butterworth filter", METH_VARARGS or METH_KEYWORDS)


val PyType_ButterworthNode = makePyType<ButterworthNode>(
    ktp_base = PyType_IIRNode.reinterpret(),
    ktp_init = initButterworthNode,
    ktp_methods = listOf(
        makeButterworth,
    )
)
