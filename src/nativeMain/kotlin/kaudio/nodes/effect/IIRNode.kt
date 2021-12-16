package kaudio.nodes.effect

import kaudio.FRAME_SIZE
import kaudio.nodes.abstract.DualNode
import kaudio.nodes.abstract.PyType_DualNode
import kaudio.utils.Configurable
import kotlinx.cinterop.*
import python.*
import pywrapper.PyObjectT
import pywrapper.builders.makePyType
import pywrapper.ext.*
import pywrapper.ext.cast
import pywrapper.ext.kt
import pywrapper.ext.pydef

class IIRNode(private val order: Int, stereo: Boolean) : DualNode(stereo) {
    private val coeffsA by attribute("coeffs_a", FloatArray(order + 1) { 0f })
    private val coeffsB by attribute("coeffs_b", FloatArray(order + 1) { 0f })

    private val x = FloatArray(order + 1) { 0f }
    private val y = FloatArray(order + 1) { 0f }

    private val x2 = FloatArray(order + 1) { 0f }
    private val y2 = FloatArray(order + 1) { 0f }

    init {
        coeffsA[0] = 1f
        coeffsB[0] = 1f
    }

    override fun processMono() {
        for (i in 0 until FRAME_SIZE) {
            var res = 0f

            for (j in 1..order) {
                val coeffA = coeffsA[j]
                val coeffB = coeffsB[j]

                res += coeffB * x[j - 1] - coeffA * y[j - 1]
            }

            res = (res + coeffsB[0] * input[i]) / coeffsA[0]

            for (j in order downTo 1) {
                x[j] = x[j - 1]
                y[j] = y[j - 1]
            }

            x[0] = input[i]
            y[0] = res

            output[i] = res
        }
    }

    override fun processStereo() {
        for (i in 0 until FRAME_SIZE) {
            var resL = 0f
            var resR = 0f

            for (j in 1..order) {
                val coeffA = coeffsA[j]
                val coeffB = coeffsB[j]

                resL += coeffB * x[j - 1] - coeffA * y[j - 1]
                resR += coeffB * x2[j - 1] - coeffA * y2[j - 1]
            }

            resL = (resL + coeffsB[0] * inputLeft[i]) / coeffsA[0]
            resR = (resR + coeffsB[0] * inputRight[i]) / coeffsA[0]

            for (j in order downTo 1) {
                x[j] = x[j - 1]
                x2[j] = x2[j - 1]
                y[j] = y[j - 1]
                y2[j] = y2[j - 1]
            }

            x[0] = inputLeft[i]
            x2[0] = inputRight[i]
            y[0] = resL
            y2[0] = resR

            outputLeft[i] = resL
            outputRight[i] = resR
        }
    }

    companion object {
        fun fromCoeffs(
            coeffsA: FloatArray,
            coeffsB: FloatArray,
            stereo: Boolean
        ) : IIRNode {
            var coeffsA = coeffsA

            if (coeffsA.size + 1 == coeffsB.size) {
                coeffsA = FloatArray(coeffsB.size) { if (it == 0) 1f else coeffsA[it-1] }
            }
            if (coeffsA.size != coeffsB.size) {
                throw IllegalArgumentException("Coeffs must have the same size")
            }

            return IIRNode(coeffsA.size - 1, stereo).apply {
                coeffsA.copyInto(this.coeffsA)
                coeffsB.copyInto(this.coeffsB)
            }
        }
    }
}

private val initIIRNode = staticCFunction { self: PyObjectT, args: PyObjectT, kwargs: PyObjectT ->
    memScoped {
        val selfObj: CPointer<KtPyObject> = self?.reinterpret() ?: return@memScoped -1

        val parsed = args.parseKw("__init__", kwargs, "order", "stereo")
        if (parsed.isEmpty()) {
            return@memScoped -1
        }

        val instance = IIRNode(parsed.arg("order"), parsed.arg("stereo"))
        val ref = StableRef.create(instance)
        selfObj.pointed.ktObject = ref.asCPointer()
        return@memScoped 0
    }
}


private fun get_PyType_IIRNode(): PyObjectT = PyType_IIRNode.ptr.reinterpret()


private val fromCoeffs = staticCFunction { dummy: PyObjectT, args: PyObjectT, kwargs: PyObjectT ->
    memScoped {

        val parsed = args.parseKw("from_coeffs", kwargs, "coeffs_b", "coeffs_a", "stereo")
        if (parsed.isEmpty()) {
            return@memScoped null
        }

        val coeffsA = parsed.arg<FloatArray>("coeffs_a")
        val coeffsB = parsed.arg<FloatArray>("coeffs_b")

        val cls = get_PyType_IIRNode()

        val obj = cls(
            Py_BuildValue("iO", coeffsA.size, if (parsed.arg("stereo")) Py_True else Py_False),
            null
        )

        val node = obj!!.kt.cast<IIRNode>()

        node.apply {
            getAttributeByName<FloatArray>("coeffs_a").set(coeffsA)
            getAttributeByName<FloatArray>("coeffs_b").set(coeffsB)
        }
        obj
    }
}.pydef("from_coeffs", "Create IIRNode from coefficients", METH_STATIC or METH_VARARGS or METH_KEYWORDS)


val PyType_IIRNode = makePyType<IIRNode>(
    ktp_base = PyType_DualNode.reinterpret(),
    ktp_init = initIIRNode,
    ktp_methods = listOf(
        fromCoeffs
    )
)
