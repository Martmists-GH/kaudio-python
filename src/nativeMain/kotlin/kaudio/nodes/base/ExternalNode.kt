package kaudio.nodes.base

import kaudio.FRAME_SIZE
import kpy.annotations.PyExport
import kpy.annotations.PyHint
import kpy.utilities.toPython
import python.PyObject_CallMethod

@PyExport
class ExternalNode(stereo: Boolean) : DualNode(stereo) {
    @delegate:PyHint
    val dataLeft by python(FloatArray(FRAME_SIZE))
    @delegate:PyHint
    val dataRight by python(FloatArray(FRAME_SIZE))
    @delegate:PyHint
    val data by python(FloatArray(FRAME_SIZE))

    override fun process() {
        if (stereo) {
            inputLeft.copyInto(dataLeft)
            inputRight.copyInto(dataRight)
            PyObject_CallMethod(this.toPython(), "processStereo", null)
            dataLeft.copyInto(outputLeft)
            dataRight.copyInto(outputRight)
        } else {
            input.copyInto(data)
            PyObject_CallMethod(this.toPython(), "processMono", null)
            data.copyInto(output)
        }
    }

    @PyExport
    public override fun processStereo() {
        throw NotImplementedError()
    }

    @PyExport
    public override fun processMono() {
        throw NotImplementedError()
    }
}
