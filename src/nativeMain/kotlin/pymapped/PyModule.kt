package pymapped

import kpy.wrappers.PyObjectT
import python.*
import kotlin.reflect.*

abstract class PyModule(name: String) {
    private val module = PyImport_GetModule(PyUnicode_FromString(name))

    object PyModuleDelegate {
        operator fun getValue(thisRef: PyModule, property: KProperty<*>): PyObjectT {
            return PyObject_GetAttrString(thisRef.module, property.name)
        }
    }

    fun getting() = PyModuleDelegate
}

class PyObjectDelegate(private val obj: PyObjectT) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): PyObjectT {
        return PyObject_GetAttrString(obj, property.name)
    }
}

fun PyObjectT.getting() = PyObjectDelegate(this)

