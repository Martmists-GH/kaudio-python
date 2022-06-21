package kaudio.ext

fun FloatArray.shift(n: Int) {
    for (j in size-1 downTo  n) {
        this[j] = this[j - n]
    }
}
