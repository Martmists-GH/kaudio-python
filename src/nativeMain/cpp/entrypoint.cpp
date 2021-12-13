#include <Python.h>
#include "libkaudio_python_api.h"

extern "C" {
PyMODINIT_FUNC PyInit_kaudio(void) {
    return (PyObject*)libkaudio_python_symbols()->kotlin.root.initialize();
}
}