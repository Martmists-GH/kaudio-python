package kaudio.ext

import platform.posix.log10f
import platform.posix.powf

val Float.amplitude: Float
    get() = powf(10f, this / 20f)

val Float.db: Float
    get() = 20f * log10f(this)
