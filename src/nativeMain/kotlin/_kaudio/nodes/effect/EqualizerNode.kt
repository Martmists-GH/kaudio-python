package _kaudio.nodes.effect

import _kaudio.nodes.abstract.DualNode
import _kaudio.nodes.abstract.PyType_DualNode
import _kaudio.nodes.util.DummyNode
import _kaudio.utils.BiquadType
import _kaudio.utils.copyStereo
import _kaudio.utils.makeBiquad
import kotlinx.cinterop.*
import platform.posix.powf
import python.KtPyObject
import pywrapper.PyObjectT
import pywrapper.builders.makePyType
import pywrapper.ext.arg
import pywrapper.ext.parseKw
import kotlin.math.roundToInt

class EqualizerNode(stereo: Boolean) : DualNode(stereo) {
    private val gain: FloatArray by attribute("gain", FloatArray(10) { 0f }) {
        nodes.forEachIndexed { index, node ->
            when (index) {
                0 -> {
                    makeBiquad(BiquadType.LOWSHELF, stereo, 31, it[index], 2f, node)
                }
                nodes.lastIndex -> {
                    makeBiquad(BiquadType.HIGHSHELF, stereo, 16383, it[index], 2f, node)
                }
                else -> {
                    makeBiquad(
                        BiquadType.PEAK,
                        stereo,
                        (32 * powf(2f, index.toFloat()) - 1).roundToInt(),
                        it[index],
                        2f,
                        node
                    )
                }
            }
        }
    }

    private val outNode = DummyNode(stereo)
    private val nodes = arrayOf(
        makeBiquad(BiquadType.LOWSHELF, stereo, 31, gain[0], 2f),
        makeBiquad(BiquadType.PEAK, stereo, 63, gain[1], 2f),
        makeBiquad(BiquadType.PEAK, stereo, 127, gain[2], 2f),
        makeBiquad(BiquadType.PEAK, stereo, 255, gain[3], 2f),
        makeBiquad(BiquadType.PEAK, stereo, 511, gain[4], 2f),
        makeBiquad(BiquadType.PEAK, stereo, 1023, gain[5], 2f),
        makeBiquad(BiquadType.PEAK, stereo, 2047, gain[6], 2f),
        makeBiquad(BiquadType.PEAK, stereo, 4095, gain[7], 2f),
        makeBiquad(BiquadType.PEAK, stereo, 8191, gain[8], 2f),
        makeBiquad(BiquadType.HIGHSHELF, stereo, 16383, gain[9], 2f),
    )

    init {
        val inputs = if (!stereo) mapOf(
            "input" to "output"
        ) else mapOf(
            "input_left" to "output_left",
            "input_right" to "output_right"
        )

        inputs.entries.forEach { (input, output) ->
            nodes.forEachIndexed { i, node ->
                if (i != nodes.lastIndex) {
                    node.connect(output, nodes[i + 1], input)
                } else {
                    node.connect(output, outNode, input)
                }
            }
        }
    }

    override fun processMono() {
        input.copyInto(nodes[0].input)
        nodes.forEach { it.process() }
        outNode.input.copyInto(output)
    }

    override fun processStereo() {
        copyStereo(inputLeft, inputRight, nodes[0].inputLeft, nodes[0].inputRight)

        nodes.forEach { it.process() }

        copyStereo(outNode.inputLeft, outNode.inputRight, outputLeft, outputRight)
    }
}

private val initEqualizerNode = staticCFunction { self: PyObjectT, args: PyObjectT, kwargs: PyObjectT ->
    memScoped {
        val selfObj: CPointer<KtPyObject> = self?.reinterpret() ?: return@memScoped -1

        val parsed = args.parseKw("__init__", kwargs, "stereo")
        if (parsed.isEmpty()) {
            return@memScoped -1
        }

        val instance = EqualizerNode(parsed.arg("stereo"))
        val ref = StableRef.create(instance)
        selfObj.pointed.ktObject = ref.asCPointer()
        return@memScoped 0
    }
}

val PyType_EqualizerNode = makePyType<EqualizerNode>(
    ktp_base = PyType_DualNode.ptr,
    ktp_init = initEqualizerNode,
)
