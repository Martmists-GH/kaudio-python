package kaudio.nodes.util

import kaudio.nodes.abstract.DualNode

class DummyNode(stereo: Boolean) : DualNode(stereo) {
    override fun processMono() {

    }

    override fun processStereo() {

    }
}
