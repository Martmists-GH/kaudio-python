import kaudio
from PySide2.QtWidgets import QWidget

from kaudio_app.nodes.abstract.base_node import BaseNode
from kaudio_app.nodes.abstract.response_node import ResponseNode


class EqualizerNode(ResponseNode):
    __identifier__ = BaseNode.__identifier__ + ".effect.basic"
    NODE_NAME = "Equalizer"

    def __init__(self):
        self.gain = [0.0] * 10
        super().__init__(True)

    def range(self):
        return -2, 2

    def get_new_node(self, stereo: bool) -> kaudio.BaseNode:
        node = kaudio.EqualizerNode(stereo)
        node.gain = self.gain
        return node

    def set_property(self, name, value):
        if name.startswith("gain_"):
            idx = int(name[5:])
            self.gain[idx] = value
            self.node.gain = self.gain
            self.plot_response()
        else:
            super().set_property(name, value)

    def configure_config_widget(self, widget: QWidget):
        super().configure_config_widget(widget)
        for i in range(10):
            self.config_float(f"gain_{i}", f"Gain ({32 * pow(2, i) - 1}Hz)", self.gain[i], (-12, 12), widget)
