package _kaudio.utils

import kotlinx.cinterop.pointed
import kotlinx.cinterop.staticCFunction
import kotlinx.cinterop.toKString
import python.PyErr_SetString
import python.PyExc_NotImplementedError
import python.PyObject_GenericGetAttr
import python.PyObject_GenericSetAttr
import pywrapper.PyObjectT
import pywrapper.builders.makePyType
import pywrapper.ext.cast
import pywrapper.ext.kt
import pywrapper.toKotlin
import pywrapper.toPython
import kotlin.collections.set
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.typeOf


abstract class Configurable {
    internal val attrs = mutableMapOf<String, Property<*>>()

    inner class Property<T : Any>(private var value: T, val type: KType, private val onSet: (T) -> Unit = {}) {
        fun get(): T {
            return value
        }

        fun set(value: T) {
            this.value = value
            onSet(value)
        }

        operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
            return value
        }

        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
            this.value = value
            onSet(value)
        }
    }

    @PublishedApi
    internal fun <T : Any> attribute(name: String, default: T, type: KType, onSet: (T) -> Unit = {}): Property<T> {
        val prop = Property(default, type, onSet)
        attrs[name] = prop
        return prop
    }

    inline fun <reified T : Any> attribute(name: String, default: T, noinline onSet: (T) -> Unit = {}): Property<T> =
        attribute(name, default, typeOf<T>(), onSet)

    fun <T : Any> getAttributeByName(name: String) = attrs[name]!! as Property<T>
}

private val initConfigurable = staticCFunction { self: PyObjectT, args: PyObjectT, kwargs: PyObjectT ->
    PyErr_SetString(
        PyExc_NotImplementedError,
        "Cannot instantiate ${self!!.pointed.ob_type!!.pointed.tp_name!!.toKString()}"
    )
    -1
}

private val getattroConfigurable = staticCFunction { self: PyObjectT, attr: PyObjectT ->
    val obj = self!!.kt.cast<Configurable>()
    val name = attr.toKotlin<String>()
    val attrObj = obj.attrs[name]

    if (attrObj != null) {
        attrObj.get().toPython()
    } else {
        PyObject_GenericGetAttr(self, attr)
    }
}

@Suppress("IMPLICIT_CAST_TO_ANY")
private val setattroConfigurable = staticCFunction { self: PyObjectT, attr: PyObjectT, value: PyObjectT ->
    val obj = self!!.kt.cast<Configurable>()
    val name = attr.toKotlin<String>()
    val attrObj = obj.attrs[name]

    if (attrObj != null) {
        val new = value.toKotlin(attrObj.type) as Any
        (attrObj as Configurable.Property<Any>).set(new)
        0
    } else {
        PyObject_GenericSetAttr(self, attr, value)
    }
}

val PyType_Configurable = makePyType<Configurable>(
    ktp_init = initConfigurable,
    ktp_getattro = getattroConfigurable,
    ktp_setattro = setattroConfigurable,
)
