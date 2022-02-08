import kaudio
from PySide2.QtWidgets import QWidget

from kaudio_app.nodes.abstract.base_node import BaseNode
from kaudio_app.nodes.abstract.response_node import ResponseNode


class ButterworthNode(ResponseNode):
    __identifier__ = BaseNode.__identifier__ + ".effect.advanced"
    NODE_NAME = "Butterworth"

    def __init__(self):
        self.type = "Lowpass"
        self.gain = 0.0
        self.frequency = 1000
        super().__init__(False)

    def get_new_node(self, stereo: bool) -> kaudio.BaseNode:
        node = kaudio.ButterworthNode(stereo)
        node.make_butterworth(self.type, self.frequency, self.gain)
        return node

    def set_property(self, name, value):
        if name == "gain":
            self.gain = value
            self.node.make_butterworth(self.type, self.frequency, self.gain)
            self.plot_response()
        elif name == "frequency":
            self.frequency = value
            self.node.make_butterworth(self.type, self.frequency, self.gain)
            self.plot_response()
        elif name == "type":
            self.type = value
            self.node.make_butterworth(self.type, self.frequency, self.gain)
            self.plot_response()
        else:
            super().set_property(name, value)
            if name == "stereo":
                self.iir_config_widget.set_node(self.node)

    def configure_config_widget(self, widget: QWidget):
        super().configure_config_widget(widget)
        self.config_float("gain", "Gain (dB)", 0.0, (-12.0, 12.0), widget)
        self.config_int("frequency", "Frequency", 1000, (1, 24000), widget)
        self.config_combobox("type", "Type", self.type, ["Lowpass", "Highpass", "Bandpass", "Notch", "Peak", "Lowshelf", "Highshelf"], widget)
