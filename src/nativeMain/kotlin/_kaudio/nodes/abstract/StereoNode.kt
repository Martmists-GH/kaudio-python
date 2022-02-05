package _kaudio.nodes.abstract

import kotlinx.cinterop.ptr
import pywrapper.builders.makePyType

abstract class StereoNode : BaseNode() {
    val inputLeft by input("input_left")
    val inputRight by input("input_right")
    val outputLeft by output("output_left")
    val outputRight by output("output_right")
}

val PyType_StereoNode = makePyType<StereoNode>(
    ktp_base = PyType_BaseNode.ptr,
)
