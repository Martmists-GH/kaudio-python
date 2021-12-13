package kaudio.utils

import kotlin.reflect.KProperty

class Delegate<T>(private var getter: () -> T) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return getter()
    }
}
