package pywrapper.ext

import kotlinx.cinterop.*
import python.PyArg_ParseTuple
import python.PyArg_ParseTupleAndKeywords
import python.PyObject
import pywrapper.PyObjectT
import pywrapper.builders.makeString
import pywrapper.toKotlin

// TODO: Not tested
fun PyObjectT.parse(name: String, n: Int): List<PyObjectT> {
    if (n > 5) {
        throw IllegalArgumentException("Not yet implemented for n > 5")
    }

    return memScoped {
        val args = List(n) { allocPointerTo<PyObject>() }
        if (n == 1 && PyArg_ParseTuple(this@parse, "O:$name", args[0].ptr) == 0) {
            return@memScoped emptyList()
        } else if (n == 2 && PyArg_ParseTuple(this@parse, "OO:$name", args[0].ptr, args[1].ptr) == 0) {
            return@memScoped emptyList()
        } else if (n == 3 && PyArg_ParseTuple(this@parse, "OOO:$name", args[0].ptr, args[1].ptr, args[2].ptr) == 0) {
            return@memScoped emptyList()
        } else if (n == 4 && PyArg_ParseTuple(
                this@parse,
                "OOOO:$name",
                args[0].ptr,
                args[1].ptr,
                args[2].ptr,
                args[3].ptr
            ) == 0
        ) {
            return@memScoped emptyList()
        } else if (n == 5 && PyArg_ParseTuple(
                this@parse,
                "OOOOO:$name",
                args[0].ptr,
                args[1].ptr,
                args[2].ptr,
                args[3].ptr,
                args[4].ptr
            ) == 0
        ) {
            return@memScoped emptyList()
        }
        return@memScoped args.map {
            it.pointed!!.ptr
        }
    }
}

fun PyObjectT.parseKw(name: String, kw: PyObjectT, vararg args: String): Map<String, PyObjectT> {
    if (args.size > 5) {
        throw IllegalArgumentException("Not yet implemented for args.size > 5")
    }

    return memScoped {
        val pyArgs = args.associateWith { allocPointerTo<PyObject>() }
        val names = allocArray<CPointerVar<ByteVar>>(args.size + 1) {
            this.value = if (it < args.size) makeString(args[it]) else null
        }
        if (args.size == 1 && PyArg_ParseTupleAndKeywords(
                this@parseKw,
                kw,
                "O:$name",
                names,
                pyArgs[args[0]]!!.ptr
            ) == 0
        ) {
            return@memScoped emptyMap()
        } else if (args.size == 2 && PyArg_ParseTupleAndKeywords(
                this@parseKw,
                kw,
                "OO:$name",
                names,
                pyArgs[args[0]]!!.ptr,
                pyArgs[args[1]]!!.ptr
            ) == 0
        ) {
            return@memScoped emptyMap()
        } else if (args.size == 3 && PyArg_ParseTupleAndKeywords(
                this@parseKw,
                kw,
                "OOO:$name",
                names,
                pyArgs[args[0]]!!.ptr,
                pyArgs[args[1]]!!.ptr,
                pyArgs[args[2]]!!.ptr
            ) == 0
        ) {
            return@memScoped emptyMap()
        } else if (args.size == 4 && PyArg_ParseTupleAndKeywords(
                this@parseKw,
                kw,
                "OOOO:$name",
                names,
                pyArgs[args[0]]!!.ptr,
                pyArgs[args[1]]!!.ptr,
                pyArgs[args[2]]!!.ptr,
                pyArgs[args[3]]!!.ptr
            ) == 0
        ) {
            return@memScoped emptyMap()
        } else if (args.size == 5 && PyArg_ParseTupleAndKeywords(
                this@parseKw,
                kw,
                "OOOOO:$name",
                names,
                pyArgs[args[0]]!!.ptr,
                pyArgs[args[1]]!!.ptr,
                pyArgs[args[2]]!!.ptr,
                pyArgs[args[3]]!!.ptr,
                pyArgs[args[4]]!!.ptr
            ) == 0
        ) {
            return@memScoped emptyMap()
        }
        return@memScoped pyArgs.mapValues { it.value.pointed!!.ptr }
    }
}

inline fun <reified T : Any> List<PyObjectT>.arg(n: Int): T {
    return this[n].toKotlin()
}

inline fun <reified T : Any> Map<String, PyObjectT>.arg(name: String): T {
    return this[name]!!.toKotlin()
}
