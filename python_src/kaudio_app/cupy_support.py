from traceback import print_exc

try:
    import cupy as backend

    def to_backend(arr):
        return backend.array(arr)

    def from_backend(arr):
        return backend.asnumpy(arr)
except ImportError as e:
    print_exc()
    print("Error importing cupy. Falling back to numpy.")

    import numpy as _backend
    from scipy import fftpack

    class numpy_scipy_proxy:
        def __getattr__(self, item):
            # if item == "fft":
            #     return fftpack
            return getattr(_backend, item)

    backend = numpy_scipy_proxy()

    def to_backend(arr):
        return arr


    def from_backend(arr):
        return arr
