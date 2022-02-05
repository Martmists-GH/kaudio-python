import _kaudio
from typing import List
from kaudio.base_nodes import DualNode, StereoNode


class VolumeNode(DualNode, _kaudio.VolumeNode):
    gain: float

    def __init__(self, stereo: bool):
        super(DualNode, self).__init__(stereo)


class IIRNode(DualNode, _kaudio.IIRNode):
    coeffs_a: List[float]
    coeffs_b: List[float]

    def __init__(self, order: int, stereo: bool):
        super(DualNode, self).__init__(order, stereo)

    @classmethod
    def from_coeffs(cls, coeffs_b: List[float], coeffs_a: List[float], stereo: bool):
        return _kaudio.IIRNode.from_coeffs(coeffs_b, coeffs_a, stereo)


class EqualLoudnessNode(DualNode, _kaudio.EqualLoudnessNode):
    def __init__(self, stereo: bool):
        super(DualNode, self).__init__(stereo)


class EqualizerNode(DualNode, _kaudio.EqualizerNode):
    gain: List[float]

    def __init__(self, stereo: bool):
        super(DualNode, self).__init__(stereo)


class Bs2bNode(StereoNode, _kaudio.Bs2bNode):
    frequency: int  # Between 300 and 2000 Hz
    feed: int    # Between 10 and 150 -> 1 dB = 10, 2 dB = 20, 3 dB = 30, ...

    def __init__(self):
        super(StereoNode, self).__init__()
