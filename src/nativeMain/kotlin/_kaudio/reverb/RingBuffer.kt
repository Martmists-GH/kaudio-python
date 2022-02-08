package _kaudio.reverb

class RingBuffer(private val size: Int) {
    private var index = 0
    private val buffer = FloatArray(size) { 0f }

    private inline fun clamp(i: Int): Int {
        return i % size
    }

    private inline fun inc(): Int {
        val current = index
        index = clamp(index + 1)
        return current
    }

    fun put(vararg values: Float) {
        for (v in values) {
            buffer[inc()] = v
        }
    }

    fun get(): Float {
        return buffer[index]
    }

    fun get(out: FloatArray) {
        for (i in 0 until size) {
            out[i] = buffer[clamp(index+i)]
        }
    }

    fun fill(value: Float) {
        for (i in 0 until size) {
            buffer[i] = value
        }
    }
}
