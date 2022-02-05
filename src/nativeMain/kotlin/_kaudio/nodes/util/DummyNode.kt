package _kaudio.nodes.util

import _kaudio.nodes.abstract.DualNode

class DummyNode(stereo: Boolean) : DualNode(stereo) {
    override fun processMono() {

    }

    override fun processStereo() {

    }
}
