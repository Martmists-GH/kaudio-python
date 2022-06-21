package kaudio.nodes.effect

import freeverb.*
import kaudio.FRAME_SIZE
import kaudio.nodes.base.StereoNode
import kotlinx.cinterop.*
import kpy.annotations.PyExport
import kpy.annotations.PyHint
import kpy.utilities.Freeable

@PyExport
class ReverbNode : StereoNode(), Freeable {
    private val model = create_revmodel();

    @delegate:PyHint
    private val roomSize by python(getroomsize(model)) {
        setroomsize(model, it)
    }
    @delegate:PyHint
    private val damp by python(getdamp(model)) {
        setdamp(model, it)
    }
    @delegate:PyHint
    private val wet by python(getwet(model)) {
        setwet(model, it)
    }
    @delegate:PyHint
    private val dry by python(getdry(model)) {
        setdry(model, it)
    }
    @delegate:PyHint
    private val width by python(getwidth(model)) {
        setwidth(model, it)
    }

    private val inLeft = nativeHeap.allocArray<FloatVar>(FRAME_SIZE)
    private val inRight = nativeHeap.allocArray<FloatVar>(FRAME_SIZE)
    private val outLeft = nativeHeap.allocArray<FloatVar>(FRAME_SIZE)
    private val outRight = nativeHeap.allocArray<FloatVar>(FRAME_SIZE)

    override fun free() {
        delete_revmodel(model)
        nativeHeap.free(inLeft)
        nativeHeap.free(inRight)
        nativeHeap.free(outLeft)
        nativeHeap.free(outRight)
    }

    override fun process() {
        val inL = inputLeft
        val inR = inputRight
        val outL = outputLeft
        val outR = outputRight

        for (i in 0 until FRAME_SIZE) {
            inLeft[i] = inL[i]
            inRight[i] = inR[i]
        }

        processreplace(model, inLeft, inRight, outLeft, outRight, FRAME_SIZE.convert(), 1)

        for (i in 0 until FRAME_SIZE) {
            outL[i] = outLeft[i]
            outR[i] = outRight[i]
        }
    }
}

