package _kaudio.nodes.abstract

import kotlinx.cinterop.ptr
import pywrapper.builders.makePyType

abstract class MonoNode : BaseNode() {
    val input by input("input")
    val output by output("output")
}

val PyType_MonoNode = makePyType<MonoNode>(
    ktp_base = PyType_BaseNode.ptr,
)
