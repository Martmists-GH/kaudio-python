package pywrapper

import kotlinx.cinterop.convert
import kotlinx.cinterop.invoke
import kotlinx.cinterop.toKStringFromUtf8
import python.*
import kotlin.reflect.KClass
import kotlin.reflect.KType

inline fun <reified R : Any> PyObjectT.toKotlin() : R = toKotlin(R::class)

@Suppress("IMPLICIT_CAST_TO_ANY")
fun <R : Any> PyObjectT.toKotlin(type: KClass<R>) : R {
    return when (type) {
        Int::class -> PyLong_AsLong(this).toInt()
        Long::class -> PyLong_AsLong(this)
        Float::class -> PyFloat_AsDouble(this).toFloat()
        Double::class -> PyFloat_AsDouble(this)
        String::class -> _PyUnicode_AsString!!.invoke(this)!!.toKStringFromUtf8()
        Boolean::class -> PyObject_IsTrue(this) == 1
        FloatArray::class -> {
            val size = PyList_Size(this)
            FloatArray(size.convert()) { i ->
                PyList_GetItem(this, i.convert()).toKotlin()
            }
        }
        IntArray::class -> {
            val size = PyList_Size(this)
            IntArray(size.convert()) { i ->
                PyList_GetItem(this, i.convert()).toKotlin()
            }
        }
        else -> throw IllegalArgumentException("Unsupported type: ${type.simpleName}")
    } as R
}

fun <T : Any> T.toPython() : PyObjectT {
    return when (this) {
        is Int -> PyLong_FromLong(this.toLong())
        is Long -> PyLong_FromLong(this)
        is Float -> PyFloat_FromDouble(this.toDouble())
        is Double -> PyFloat_FromDouble(this)
        is String -> PyUnicode_FromString(this)
        is Boolean -> PyBool_FromLong(if (this) 1 else 0)
        is FloatArray -> {
            val list = PyList_New(this.size.convert())
            for (i in 0 until this.size) {
                PyList_SetItem(list, i.toLong(), this[i].toPython())
            }
            list
        }
        is IntArray -> {
            val list = PyList_New(this.size.convert())
            for (i in 0 until this.size) {
                PyList_SetItem(list, i.toLong(), this[i].toPython())
            }
            list
        }
        else -> throw IllegalArgumentException("Unsupported type: ${this::class.simpleName}")
    }
}