package kaudio.nodes.effect

import kaudio.nodes.base.BaseNode
import kaudio.nodes.base.DualNode
import kpy.annotations.PyExport
import kpy.utilities.Freeable

@PyExport
class EqualLoudnessNode(stereo: Boolean) : DualNode(stereo), Freeable {
    private val preAmp = VolumeNode(stereo).also {
        it.gainDb = 10f
    }

    private val butterworth = IIRNode(2, stereo).also {
        it.coeffsA = floatArrayOf(
            1f,
            -1.9722334f,
            0.9726137f,
        )
        it.coeffsB = floatArrayOf(
            0.9862118f,
            -1.9724236f,
            0.9862118f,
        )
    }

    private val yulewalk = IIRNode(10, stereo).also {
        it.coeffsA = floatArrayOf(
            1f,
            -3.846646f,
            7.8150167f,
            -11.341703f,
            13.055042f,
            -12.287599f,
            9.482938f,
            -5.8725786f,
            2.7546587f,
            -0.8698438f,
            0.13919315f,
        )
        it.coeffsB = floatArrayOf(
            0.038575996f,
            -0.021603672f,
            -0.0012339532f,
            -9.291678E-5f,
            -0.016552603f,
            0.021615269f,
            -0.020740451f,
            0.0059429808f,
            0.0030642801f,
            1.2025322E-4f,
            0.0028846369f,
        )
    }

    init {
        if (stereo) {
            preAmp.connect("outputLeft", butterworth, "inputLeft")
            preAmp.connect("outputRight", butterworth, "inputRight")
            butterworth.connect("outputLeft", yulewalk, "inputLeft")
            butterworth.connect("outputRight", yulewalk, "inputRight")
        } else {
            preAmp.connect("output", butterworth, "input")
            butterworth.connect("output", yulewalk, "input")
        }
    }

    override fun processMono() {
        input.copyInto(preAmp.input)

        preAmp.process()
        butterworth.process()
        yulewalk.process()
    }

    override fun processStereo() {
        inputLeft.copyInto(preAmp.inputLeft)
        inputRight.copyInto(preAmp.inputRight)

        preAmp.process()
        butterworth.process()
        yulewalk.process()
    }

    override fun connect(output: String, node: BaseNode, input: String) {
        when (output) {
            "output" -> {
                yulewalk.connect("output", node, input)
            }
            "outputLeft" -> {
                yulewalk.connect("outputLeft", node, input)
            }
            "outputRight" -> {
                yulewalk.connect("outputRight", node, input)
            }
            else -> super.connect(output, node, input)
        }
    }

    override fun free() {
        butterworth.free()
        yulewalk.free()
    }
}
