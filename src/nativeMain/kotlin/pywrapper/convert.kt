package pywrapper

import kotlinx.cinterop.*
import python.*
import kotlin.reflect.KClass

inline fun <reified R : Any> PyObjectT.toKotlin() : R = toKotlin(R::class)
inline fun <reified T : Any> PyObjectT.toKotlinList() : List<T> = toKotlinList(T::class)
inline fun <reified K : Any, reified V : Any> PyObjectT.toKotlinMap() : Map<K, V> = toKotlinMap(K::class, V::class)

fun <T : Any> PyObjectT.toKotlinList(type: KClass<T>) : List<T> {
    val size = PyList_Size(this)
    return (0 until size).map { PyList_GetItem(this, it).toKotlin(type) }
}

fun <K : Any, V : Any> PyObjectT.toKotlinMap(kType: KClass<K>, vType: KClass<V>) : Map<K, V> {
    val list = PyDict_Keys(this).toKotlinList<CPointer<PyObject>>()
    return list.associate {
        it.toKotlin(kType) to PyDict_GetItem(this, it).toKotlin(vType)
    }
}

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
        CPointer::class, CValuesRef::class -> {
            // Assume PyObjectT
            this
        }
        else -> throw IllegalArgumentException("Unsupported type: ${type.simpleName}")
    } as R
}

fun <T> T.toPython() : PyObjectT {
    return when (this) {
        null -> Py_None
        is Int -> PyLong_FromLong(this.toLong())
        is Long -> PyLong_FromLong(this)
        is Float -> PyFloat_FromDouble(this.toDouble())
        is Double -> PyFloat_FromDouble(this)
        is String -> PyUnicode_FromString(this)
        is Boolean -> PyBool_FromLong(if (this) 1 else 0)
        is FloatArray -> {
            val list = PyList_New(this.size.convert())
            for (i in 0 until this.size) {
                PyList_SetItem(list, i.convert(), this[i].toPython())
            }
            list
        }
        is IntArray -> {
            val list = PyList_New(this.size.convert())
            for (i in 0 until this.size) {
                PyList_SetItem(list, i.convert(), this[i].toPython())
            }
            list
        }
        is List<*> -> {
            val list = PyList_New(this.size.convert())
            for (i in 0 until this.size) {
                PyList_SetItem(list, i.convert(), this[i].toPython())
            }
            list
        }
        is Map<*, *> -> {
            val dict = PyDict_New()
            for ((k, v) in this) {
                PyDict_SetItem(dict, k.toPython(), v.toPython())
            }
            dict
        }
        else -> throw IllegalArgumentException("Unsupported type: ${this!!::class.simpleName}")
    }
}