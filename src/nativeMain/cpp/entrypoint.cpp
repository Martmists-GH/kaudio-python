#include <Python.h>
#include "libkaudio_python_api.h"

extern "C" {
PyMODINIT_FUNC PyInit__kaudio(void) {  // extra underscore since we export as _kaudio
    return (PyObject*)libkaudio_python_symbols()->kotlin.root.initialize();
}
}
