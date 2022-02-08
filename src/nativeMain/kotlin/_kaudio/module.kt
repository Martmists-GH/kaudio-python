package _kaudio

import _kaudio.nodes.abstract.*
import _kaudio.nodes.effect.*
import _kaudio.nodes.util.*
import _kaudio.utils.PyType_Configurable
import kotlinx.cinterop.ptr
import python.PYTHON_API_VERSION
import python.PyModule_Create2
import pywrapper.PyObjectT
import pywrapper.builders.makeModule
import pywrapper.ext.addType

const val FRAME_SIZE = 1024

val mod = makeModule(
    km_name = "_kaudio",
)

fun createPyKtModule(): PyObjectT {
    val obj = PyModule_Create2(mod.ptr, PYTHON_API_VERSION)

    if (obj.addType(PyType_Configurable) < 0) return null

    if (obj.addType(PyType_BaseNode) < 0) return null
    if (obj.addType(PyType_MonoNode) < 0) return null
    if (obj.addType(PyType_StereoNode) < 0) return null
    if (obj.addType(PyType_DualNode) < 0) return null

    if (obj.addType(PyType_StereoSync) < 0) return null
    if (obj.addType(PyType_SplitterNode) < 0) return null
    if (obj.addType(PyType_CombinerNode) < 0) return null

    if (obj.addType(PyType_InputNode) < 0) return null
    if (obj.addType(PyType_OutputNode) < 0) return null

    if (obj.addType(PyType_IIRNode) < 0) return null
    if (obj.addType(PyType_ButterworthNode) < 0) return null
    if (obj.addType(PyType_VolumeNode) < 0) return null
    if (obj.addType(PyType_EqualizerNode) < 0) return null
    if (obj.addType(PyType_EqualLoudnessNode) < 0) return null
    if (obj.addType(PyType_Bs2bNode) < 0) return null
    if (obj.addType(PyType_ReverbNode) < 0) return null
    if (obj.addType(PyType_TubeSimulatorNode) < 0) return null

    return obj
}
