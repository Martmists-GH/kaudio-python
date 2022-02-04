package kaudio.nodes.effect

import kaudio.FRAME_SIZE
import kaudio.nodes.abstract.DualNode
import kaudio.nodes.abstract.PyType_DualNode
import kaudio.nodes.util.DummyNode
import kotlinx.cinterop.*
import python.*
import pywrapper.PyObjectT
import pywrapper.builders.makePyType
import pywrapper.ext.arg
import pywrapper.ext.parseKw

class EqualLoudnessNode(stereo: Boolean) : DualNode(stereo) {
    private val preAmp = VolumeNode(stereo).also {
        (it.attrs["gain"] as Property<Float>).set(10f)
    }

    private val butterworth = IIRNode.fromCoeffs(
        floatArrayOf(
            1f,
            -1.9722334f,
            0.9726137f,
        ),
        floatArrayOf(
            0.9862118f,
            -1.9724236f,
            0.9862118f,
        ),
        stereo,
    )

    private val yulewalk = IIRNode.fromCoeffs(
        floatArrayOf(
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
        ),
        floatArrayOf(
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
        ),
        stereo,
    )

    private val dummy = DummyNode(stereo)

    init {
        if (stereo) {
            preAmp.connect("output_left", butterworth, "input_left")
            preAmp.connect("output_right", butterworth, "input_right")
            butterworth.connect("output_left", yulewalk, "input_left")
            butterworth.connect("output_right", yulewalk, "input_right")
            yulewalk.connect("output_left", dummy, "input_left")
            yulewalk.connect("output_right", dummy, "input_right")
        } else {
            preAmp.connect("output", butterworth, "input")
            butterworth.connect("output", yulewalk, "input")
            yulewalk.connect("output", dummy, "input")
        }
    }

    override fun processMono() {
        input.copyInto(preAmp.input)

        preAmp.process()
        butterworth.process()
        yulewalk.process()

        dummy.input.copyInto(output)
    }

    override fun processStereo() {
        val preL = preAmp.inputLeft
        val preR = preAmp.inputRight
        val inL = inputLeft
        val inR = inputRight

        for (i in 0 until FRAME_SIZE) {
            preL[i] = inL[i]
            preR[i] = inR[i]
        }

        preAmp.process()
        butterworth.process()
        yulewalk.process()

        val outL = outputLeft
        val outR = outputRight
        val dummyL = dummy.inputLeft
        val dummyR = dummy.inputRight

        for (i in 0 until FRAME_SIZE) {
            outL[i] = dummyL[i]
            outR[i] = dummyR[i]
        }
    }
}

private val initEqualLoudnessNode = staticCFunction { self: PyObjectT, args: PyObjectT, kwargs: PyObjectT ->
    memScoped {
        val selfObj: CPointer<KtPyObject> = self?.reinterpret() ?: return@memScoped -1

        val parsed = args.parseKw("__init__", kwargs, "stereo")
        if (parsed.isEmpty()) {
            return@memScoped -1
        }

        val instance = EqualLoudnessNode(parsed.arg("stereo"))
        val ref = StableRef.create(instance)
        selfObj.pointed.ktObject = ref.asCPointer()
        return@memScoped 0
    }
}

val PyType_EqualLoudnessNode = makePyType<EqualLoudnessNode>(
    ktp_base = PyType_DualNode.ptr,
    ktp_init = initEqualLoudnessNode
)
