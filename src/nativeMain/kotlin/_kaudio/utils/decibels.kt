package _kaudio.utils

import platform.posix.log10
import platform.posix.pow

fun dbToAmp(db: Float): Float {
    return pow(10.0, db / 20.0).toFloat()
}

fun ampToDb(amp: Float): Float {
    return 20f * log10(amp.toDouble()).toFloat()
}
