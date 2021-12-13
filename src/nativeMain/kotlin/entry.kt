import kaudio.createPyKtModule
import kotlinx.cinterop.CPointer
import python.PyObject

fun initialize(): CPointer<PyObject>? {
    return createPyKtModule()
}
