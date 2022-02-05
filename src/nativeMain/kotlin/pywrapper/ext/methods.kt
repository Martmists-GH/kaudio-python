package pywrapper.ext

import kotlinx.cinterop.CValue
import kotlinx.cinterop.cValue
import python.METH_KEYWORDS
import python.METH_NOARGS
import python.METH_VARARGS
import python.PyMethodDef
import pywrapper.FuncPtr
import pywrapper.PyMethodKwargsT
import pywrapper.PyMethodT
import pywrapper.builders.makeString

internal inline fun FuncPtr<PyMethodT>.pydef(
    name: String,
    doc: String,
    flags: Int = METH_NOARGS
): CValue<PyMethodDef> {
    return cValue {
        ml_name = makeString(name)
        ml_doc = makeString(doc)
        ml_flags = flags
        ml_meth = this@pydef
    }
}

internal inline fun FuncPtr<PyMethodKwargsT>.pydef(
    name: String,
    doc: String,
    flags: Int = METH_VARARGS or METH_KEYWORDS
): CValue<PyMethodDef> {
    return cValue {
        ml_name = makeString(name)
        ml_doc = makeString(doc)
        ml_flags = flags
        ml_meth = this@pydef as FuncPtr<PyMethodT>
    }
}
