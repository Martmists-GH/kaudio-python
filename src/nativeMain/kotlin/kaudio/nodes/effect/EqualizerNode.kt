package kaudio.nodes.effect

import kaudio.nodes.base.BaseNode
import kaudio.nodes.base.DualNode
import kaudio.utils.BiquadType
import kaudio.utils.makeBiquad
import kpy.annotations.PyExport
import kpy.annotations.PyHint

@PyExport
class EqualizerNode(stereo: Boolean) : DualNode(stereo) {
    private val freqMap = listOf(31, 62, 125, 250, 500, 1000, 2000, 4000, 8000, 16000)

    @delegate:PyHint
    private val gain by python(FloatArray(10) { 0f }) {
        if (it.size != 10) {
            throw IllegalArgumentException("Gain array must be of size 10")
        }

        it.forEachIndexed { index, g ->
            val coeffs = makeBiquad(BiquadType.PEAK, freqMap[index], g, 1.41f)
            nodes[index].coeffsA = coeffs.first
            nodes[index].coeffsB = coeffs.second
        }
    }

    private val nodes = List(10) {
        IIRNode(2, stereo)
    }

    init {
        for (i in 0 until 9) {
            val current = nodes[i]
            val next = nodes[i + 1]
            if (stereo) {
                current.connect("outputLeft", next, "inputLeft")
                current.connect("outputRight", next, "inputRight")
            } else {
                current.connect("output", next, "input")
            }
        }
    }

    override fun processStereo() {
        inputLeft.copyInto(nodes[0].inputLeft)
        inputRight.copyInto(nodes[0].inputRight)

        for (node in nodes) {
            node.process()
        }
    }

    override fun processMono() {
        input.copyInto(nodes[0].input)

        for (node in nodes) {
            node.process()
        }
    }

    override fun connect(output: String, node: BaseNode, input: String) {
        when (output) {
            "output" -> {
                nodes.last().connect("output", node, input)
            }
            "outputLeft" -> {
                nodes.last().connect("outputLeft", node, input)
            }
            "outputRight" -> {
                nodes.last().connect("outputRight", node, input)
            }
            else -> super.connect(output, node, input)
        }
    }
}
