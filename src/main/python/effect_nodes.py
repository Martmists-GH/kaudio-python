import _kaudio
from typing import List
from kaudio.base_nodes import DualNode, StereoNode


class VolumeNode(DualNode, _kaudio.VolumeNode):
    """
    Adjusts the volume of the input signal.
    """
    gain: float

    def __init__(self, stereo: bool):
        super(DualNode, self).__init__(stereo)


class IIRNode(DualNode, _kaudio.IIRNode):
    """
    Applies an N-order IIR filter to the input signal.
    """
    coeffs_a: List[float]  # Denominator coefficients
    coeffs_b: List[float]  # Numerator coefficients

    def __init__(self, order: int, stereo: bool):
        super(DualNode, self).__init__(order, stereo)

    @classmethod
    def from_coeffs(cls, coeffs_b: List[float], coeffs_a: List[float], stereo: bool):
        return _kaudio.IIRNode.from_coeffs(coeffs_b, coeffs_a, stereo)


class ButterworthNode(IIRNode, _kaudio.ButterworthNode):
    """
    Applies a 2nd-order Butterworth filter to the input signal.
    """
    def __init__(self, stereo: bool):
        super(IIRNode, self).__init__(stereo)

    def make_butterworth(self, type: str, freq: int, gain: float):
        """
        Valid types:
                Lowpass
                Highpass
                Bandpass
                Notch
                Peak
                Lowshelf
                Highshelf
        """
        super(IIRNode, self).make_butterworth(type, freq, gain)


class EqualLoudnessNode(DualNode, _kaudio.EqualLoudnessNode):
    """
    Applies equal loudness filter to the input signal.
    """
    def __init__(self, stereo: bool):
        super(DualNode, self).__init__(stereo)


class EqualizerNode(DualNode, _kaudio.EqualizerNode):
    """
    Applies a shelf/peaking equalizer to the input signal.
    """

    gain: List[float]  # Gain for each band

    def __init__(self, stereo: bool):
        super(DualNode, self).__init__(stereo)


class Bs2bNode(StereoNode, _kaudio.Bs2bNode):
    """
    Applies a Bauer Stereophonic to Binaural (BS2B) filter to the input signal.
    """
    frequency: int  # Between 300 and 2000 Hz
    feed: int       # Between 10 and 150 -> 1 dB = 10, 2 dB = 20, 3 dB = 30, ...

    def __init__(self):
        super(StereoNode, self).__init__()


class TubeSimulatorNode(DualNode, _kaudio.TubeSimulatorNode):
    """
    Applies a tube simulator (6N1J) filter to the input signal.
    """
    def __init__(self, stereo: bool):
        super(DualNode, self).__init__(stereo)


class ReverbNode(StereoNode, _kaudio.ReverbNode):
    """
    Applies a reverb filter to the input signal.
    """
    room_size: float  # Between 0 and 1, represents reflectivity
    damp: float       # Between 0 and 1, represents damping factor
    wet: float        # Between 0 and 1, represents wet factor
    dry: float        # Between 0 and 1, represents dry factor
    width: float      # Between 0 and 1, represents mixing level [0 = each reverb directly, 1 = all reverb mixed]

    def __init__(self):
        super(StereoNode, self).__init__()
