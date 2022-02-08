package _kaudio.reverb

class AllpassFilter(size: Int) {
    private val buffer = RingBuffer(size)
    var feedback = 0f

    fun mute() {
        buffer.fill(0f)
    }

    fun process(sample: Float): Float {
        val outSample = buffer.get()
        buffer.put(sample + feedback * outSample)
        return outSample - sample
    }
}
