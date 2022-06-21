package kaudio.nodes.base

import kaudio.FRAME_SIZE
import kaudio.utils.PythonAttrs
import kpy.annotations.PyExport
import kotlin.reflect.KProperty

@PyExport
abstract class BaseNode : PythonAttrs() {
    companion object {
        private val DUMMY_ARRAY = FloatArray(FRAME_SIZE)
    }

    protected class InputDelegate(private val arr: FloatArray) {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): FloatArray {
            return arr
        }
    }

    protected object InputProvider {
        operator fun provideDelegate(thisRef: BaseNode, prop: KProperty<*>) : InputDelegate {
            return InputDelegate(FloatArray(FRAME_SIZE)).also {
                thisRef.inputs[prop.name] = it
            }
        }
    }

    protected object OutputDelegate {
        operator fun getValue(thisRef: BaseNode, property: KProperty<*>): FloatArray {
            return thisRef.outputs[property.name]?.getValue(thisRef, property) ?: DUMMY_ARRAY
        }
    }

    protected object OutputProvider {
        operator fun provideDelegate(thisRef: BaseNode, prop: KProperty<*>) : OutputDelegate {
            return OutputDelegate.also {
                thisRef.outputs[prop.name] = null
            }
        }
    }

    protected val inputs = mutableMapOf<String, InputDelegate>()
    protected val outputs = mutableMapOf<String, InputDelegate?>()

    protected fun input() = InputProvider
    protected fun output() = OutputProvider

    protected fun removeInput(property: KProperty<FloatArray>) = inputs.remove(property.name)
    protected fun removeOutput(property: KProperty<FloatArray>) = outputs.remove(property.name)

    @PyExport
    open fun connect(output: String, node: BaseNode, input: String) {
        if (output !in outputs) {
            throw IllegalArgumentException("Output $output does not exist on ${this::class.simpleName}")
        }
        outputs[output] = node.inputs[input]!!
    }

    @PyExport
    fun disconnect(output: String) {
        if (output !in outputs) {
            throw IllegalArgumentException("Output $output does not exist on ${this::class.simpleName}")
        }
        outputs[output] = null
    }

    @PyExport
    fun inputs(): List<String> {
        return inputs.keys.toList()
    }

    @PyExport
    fun outputs(): List<String> {
        return outputs.keys.toList()
    }

    @PyExport
    abstract fun process()
}
