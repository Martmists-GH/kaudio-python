from kaudio.app.nodes.base_nodes import StereoNode, DualNode
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


class Bs2bNode(StereoNode[Bs2bNodeImpl]):
    __identifier__ = "Effect"
    NODE_NAME = "Bs2b"

    def __init__(self):
        tmp = Bs2bNodeImpl()
        super().__init__(config={"frequency": tmp.frequency, "feed": tmp.feed}, has_widget=True)
        self.config_int("frequency", "Cut Frequency (Hz)", self.config["frequency"], (300, 2000, 50))
        self.config_float("feed", "Feed level (dB)", self.config["feed"] / 10, (1, 15, 0.5))

    def set_frequency(self, val: int):
        self.config["frequency"] = val
        self.k_node.frequency = val

    def set_feed(self, val: float):
        self.config["feed"] = val * 10
        self.k_node.feed = val * 10

    def get_node(self, stereo: bool) -> Bs2bNodeImpl:
        return Bs2bNodeImpl()


class EqualizerNode(DualNode[EqualizerNodeImpl]):
    __identifier__ = "Effect"
    NODE_NAME = "Equalizer"
    frequencies = [31, 62, 125, 250, 500, 1000, 2000, 4000, 8000, 16000]

    def __init__(self):
        tmp = EqualizerNodeImpl(False)
        super().__init__(config={"gain": tmp.gain}, has_widget=True)
        self.add_tab("response")
        for freq in EqualizerNode.frequencies:
            self.config_float(f"gain_{freq}", f"Gain ({freq} Hz)", 0, (-12, 12, 0.1))

    def set_gain(self, bin_idx: int, val: float):
        self.config["gain"][bin_idx] = val
        self.k_node.gain = self.config["gain"]
        self.update_plot()

    def set_gain_31(self, val: float):
        self.set_gain(0, val)

    def set_gain_62(self, val: float):
        self.set_gain(1, val)

    def set_gain_125(self, val: float):
        self.set_gain(2, val)

    def set_gain_250(self, val: float):
        self.set_gain(3, val)

    def set_gain_500(self, val: float):
        self.set_gain(4, val)

    def set_gain_1000(self, val: float):
        self.set_gain(5, val)

    def set_gain_2000(self, val: float):
        self.set_gain(6, val)

    def set_gain_4000(self, val: float):
        self.set_gain(7, val)

    def set_gain_8000(self, val: float):
        self.set_gain(8, val)

    def set_gain_16000(self, val: float):
        self.set_gain(9, val)

    def get_node(self, stereo: bool) -> EqualizerNodeImpl:
        return EqualizerNodeImpl(stereo)


class EqualLoudnessNode(DualNode[EqualLoudnessNodeImpl]):
    __identifier__ = "Effect"
    NODE_NAME = "Equal Loudness"

    def __init__(self):
        super().__init__()
        self.add_tab("response")

    def get_node(self, stereo: bool) -> EqualLoudnessNodeImpl:
        return EqualLoudnessNodeImpl(stereo)


class FIRNode(DualNode[FIRNodeImpl]):
    __identifier__ = "Effect"
    NODE_NAME = "FIR Filter"

    def __init__(self):
        self.order = 2
        tmp = FIRNodeImpl(self.order, False)
        super().__init__(config={"coeffs": tmp.coeffs}, has_widget=True)
        self.add_tab("response")
        # TODO: Add configuration

    def get_node(self, stereo: bool) -> FIRNodeImpl:
        return FIRNodeImpl(self.order, stereo)


class IIRNode(DualNode[IIRNodeImpl]):
    __identifier__ = "Effect"
    NODE_NAME = "IIR Filter"

    def __init__(self):
        self.order = 2
        tmp = IIRNodeImpl(self.order, False)
        super().__init__(config={"coeffsA": tmp.coeffsA, "coeffsB": tmp.coeffsB}, has_widget=True)
        self.add_tab("response")
        # TODO: Add configuration

    def get_node(self, stereo: bool) -> IIRNodeImpl:
        return IIRNodeImpl(self.order, stereo)


class ReverbNode(StereoNode[ReverbNodeImpl]):
    __identifier__ = "Effect"
    NODE_NAME = "Reverb"

    def __init__(self):
        tmp = ReverbNodeImpl()
        super().__init__(config={
            "dry": tmp.dry, "wet": tmp.wet, "damp": tmp.damp,
            "width": tmp.width, "roomSize": tmp.roomSize
        }, has_widget=True)
        self.config_float("room_size", "Room size", self.config["roomSize"], (0, 1, 0.05))
        self.config_float("damp", "Damping", self.config["damp"], (0, 1, 0.05))
        self.config_float("width", "Width", self.config["width"], (0, 1, 0.05))
        self.config_float("dry", "Dry signal", self.config["dry"], (0, 1, 0.05))
        self.config_float("wet", "Wet signal", self.config["wet"], (0, 1, 0.05))

    def set_room_size(self, val: float):
        self.config["roomSize"] = val
        self.k_node.roomSize = val

    def set_damp(self, val: float):
        self.config["damp"] = val
        self.k_node.damp = val

    def set_width(self, val: float):
        self.config["width"] = val
        self.k_node.width = val

    def set_dry(self, val: float):
        self.config["dry"] = val
        self.k_node.dry = val

    def set_wet(self, val: float):
        self.config["wet"] = val
        self.k_node.wet = val

    def get_node(self, stereo: bool) -> ReverbNodeImpl:
        return ReverbNodeImpl()


class TubeSimulatorNode(DualNode[TubeSimulatorNodeImpl]):
    __identifier__ = "Effect"
    NODE_NAME = "Tube Simulator"

    def __init__(self):
        super().__init__()

    def get_node(self, stereo: bool) -> TubeSimulatorNodeImpl:
        return TubeSimulatorNodeImpl(stereo)


class VolumeNode(DualNode[VolumeNodeImpl]):
    __identifier__ = "Effect"
    NODE_NAME = "Volume"

    def __init__(self):
        tmp = VolumeNodeImpl(False)
        super().__init__(config={"gainDb": tmp.gain}, has_widget=True)
        self.config_float("gain", "Gain (dB)", self.config["gainDb"], (-12, 12, 0.5))

    def set_gain(self, value: float):
        self.config["gainDb"] = value
        self.k_node.gainDb = value

    def get_node(self, stereo: bool) -> VolumeNodeImpl:
        return VolumeNodeImpl(stereo)
