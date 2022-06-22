from typing import Any

from kaudio.app.nodes.base_nodes import StereoNode, DualNode
from kaudio.nodes.base import BaseNode as KBaseNode
from kaudio.nodes.effect import (
    Bs2bNode as Bs2bNodeImpl,
    EqualizerNode as EqualizerNodeImpl,
    EqualLoudnessNode as EqualLoudnessNodeImpl,
    FIRNode as FIRNodeImpl,
    IIRNode as IIRNodeImpl,
    ReverbNode as ReverbNodeImpl,
    TubeSimulatorNode as TubeSimulatorNodeImpl,
    VolumeNode as VolumeNodeImpl,
)


class Bs2bNode(StereoNode):
    __identifier__ = "Effect"
    NODE_NAME = "Bs2b"

    def __init__(self):
        super().__init__()

    def get_node(self, stereo: bool) -> KBaseNode:
        return Bs2bNodeImpl()


class EqualizerNode(DualNode):
    __identifier__ = "Effect"
    NODE_NAME = "Equalizer"

    def __init__(self):
        super().__init__()

    def get_node(self, stereo: bool) -> KBaseNode:
        return EqualizerNodeImpl(stereo)


class EqualLoudnessNode(DualNode):
    __identifier__ = "Effect"
    NODE_NAME = "Equal Loudness"

    def __init__(self):
        super().__init__()

    def get_node(self, stereo: bool) -> KBaseNode:
        return EqualLoudnessNodeImpl(stereo)


class FIRNode(DualNode):
    __identifier__ = "Effect"
    NODE_NAME = "FIR Filter"

    def __init__(self):
        self.order = 2

        super().__init__()
        self.add_text_input("order", "Order")

    def set_property(self, name: str, value: Any, push_undo: bool = True):
        super().set_property(name, value, push_undo)
        if name == "order":
            self.order = int(value)
            self.k_node = self.get_node(self.is_stereo)

    def get_node(self, stereo: bool) -> KBaseNode:
        return FIRNodeImpl(self.order, stereo)


class IIRNode(DualNode):
    __identifier__ = "Effect"
    NODE_NAME = "IIR Filter"

    def __init__(self):
        self.order = 2
        super().__init__()
        self.add_text_input("order", "Order")

    def set_property(self, name: str, value: Any, push_undo: bool = True):
        super().set_property(name, value, push_undo)
        if name == "order":
            self.order = int(value)
            self.k_node = self.get_node(self.is_stereo)

    def get_node(self, stereo: bool) -> KBaseNode:
        return IIRNodeImpl(self.order, stereo)


class ReverbNode(StereoNode):
    __identifier__ = "Effect"
    NODE_NAME = "Reverb"

    def __init__(self):
        super().__init__()

    def get_node(self, stereo: bool) -> KBaseNode:
        return ReverbNodeImpl()


class TubeSimulatorNode(DualNode):
    __identifier__ = "Effect"
    NODE_NAME = "Tube Simulator"

    def __init__(self):
        super().__init__()

    def get_node(self, stereo: bool) -> KBaseNode:
        return TubeSimulatorNodeImpl(stereo)


class VolumeNode(DualNode):
    __identifier__ = "Effect"
    NODE_NAME = "Volume"

    def __init__(self):
        super().__init__()

    def get_node(self, stereo: bool) -> KBaseNode:
        return VolumeNodeImpl(stereo)
