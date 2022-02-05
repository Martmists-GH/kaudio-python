package _kaudio.utils

import _kaudio.FRAME_SIZE

fun copyStereo(inL: FloatArray, inR: FloatArray, outL: FloatArray, outR: FloatArray, length: Int = FRAME_SIZE) {
    for (i in 0 until length) {
        outL[i] = inL[i]
        outR[i] = inR[i]
    }
}
