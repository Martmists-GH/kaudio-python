package kaudio

import kaudio.nodes.abstract.PyType_BaseNode
import kaudio.nodes.abstract.PyType_DualNode
import kaudio.nodes.abstract.PyType_MonoNode
import kaudio.nodes.abstract.PyType_StereoNode
import kaudio.nodes.effect.PyType_EqualLoudnessNode
import kaudio.nodes.effect.PyType_IIRNode
import kaudio.nodes.effect.PyType_VolumeNode
import kaudio.nodes.util.*
import kaudio.utils.PyType_Configurable
import kotlinx.cinterop.ptr
import python.PYTHON_API_VERSION
import python.PyModule_Create2
import pywrapper.PyObjectT
import pywrapper.builders.makeModule
import pywrapper.ext.addType

const val FRAME_SIZE = 1024

val mod = makeModule(
    km_name = "kaudio",
)

fun createPyKtModule(): PyObjectT {
    val obj = PyModule_Create2(mod.ptr, PYTHON_API_VERSION)

    if (obj.addType(PyType_Configurable) < 0) return null

    if (obj.addType(PyType_BaseNode) < 0) return null
    if (obj.addType(PyType_MonoNode) < 0) return null
    if (obj.addType(PyType_StereoNode) < 0) return null
    if (obj.addType(PyType_DualNode) < 0) return null

    if (obj.addType(PyType_StereoSync) < 0) return null
    if (obj.addType(PyType_Mono2StereoNode) < 0) return null
    if (obj.addType(PyType_Stereo2MonoNode) < 0) return null

    if (obj.addType(PyType_InputNode) < 0) return null
    if (obj.addType(PyType_OutputNode) < 0) return null

    if (obj.addType(PyType_IIRNode) < 0) return null
    if (obj.addType(PyType_VolumeNode) < 0) return null
    if (obj.addType(PyType_EqualLoudnessNode) < 0) return null

    return obj
}
