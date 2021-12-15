package pywrapper.ext

import kotlinx.cinterop.*
import python.PyArg_Parse
import python.PyObject
import pywrapper.PyObjectT

// TODO: Not tested
fun PyObjectT.parse(n: Int) : List<PyObjectT> {
    return memScoped {
        val args = List(n) { allocPointerTo<PyObject>() }
        if (PyArg_Parse(this@parse, "O".repeat(n), *args.map(CPointerVar<PyObject>::ptr).toTypedArray()) == 0) {
            return@memScoped emptyList()
        }
        return@memScoped args.map {
            it.pointed!!.ptr
        }
    }
}
