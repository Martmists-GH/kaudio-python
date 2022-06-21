package kaudio.utils

import kpy.annotations.PyExport
import kpy.annotations.PyMagic
import kpy.annotations.PyMagicMethod
import kpy.utilities.toKotlin
import kpy.utilities.toPython
import kpy.wrappers.PyObjectT
import python.PyObject_GenericGetAttr
import python.PyObject_GenericSetAttr
import kotlin.reflect.*


@PyExport
abstract class PythonAttrs {
    private val kProperties = mutableMapOf<String, PyProp<*>>()
    protected fun <T> python(default: T, onSet: (T) -> Unit = {}) = PyPropProvider<T>(default, onSet)
    protected fun removeProperty(prop: KProperty<*>) = kProperties.remove(prop.name)

    @PyMagic(PyMagicMethod.TP_GETATTRO)
    internal fun __getattr__(name: String): Any? {
        val prop = kProperties[name]
        return if (prop != null) {
            prop.get()
        } else {
            PyObject_GenericGetAttr(this.toPython(), name.toPython())
        }
    }

    @PyMagic(PyMagicMethod.TP_SETATTRO)
    internal fun __setattr__(name: String, value: PyObjectT): Int {
        val prop = kProperties[name]
        return if (prop != null) {
            (prop as PyProp<Any>).set(value.toKotlin(prop.type))
            0
        } else {
            PyObject_GenericSetAttr(this.toPython(), name.toPython(), value)
        }
    }

    protected class PyPropProvider<T>(private val default: T, private val onSet: (T) -> Unit) {
        operator fun provideDelegate(thisRef: PythonAttrs, property: KProperty<*>): PyProp<T> {
            return PyProp(default, onSet, property.returnType).also {
                thisRef.kProperties[property.name] = it
            }
        }
    }

    class PyProp<T>(private var value: T, private val onSet: (T) -> Unit, val type: KType) {
        internal fun get(): T {
            return value
        }

        internal fun set(value: T) {
            this.value = value
            onSet(value)
        }

        operator fun getValue(thisRef: PythonAttrs, property: KProperty<*>): T {
            return get()
        }

        operator fun setValue(thisRef: PythonAttrs, property: KProperty<*>, value: T) {
            set(value)
        }
    }
}
