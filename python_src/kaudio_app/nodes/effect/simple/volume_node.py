import kaudio
from PySide2.QtWidgets import QWidget

from kaudio_app.nodes.abstract.base_node import BaseNode


class VolumeNode(BaseNode):
    __identifier__ = BaseNode.__identifier__ + ".effect.basic"
    NODE_NAME = "Volume"

    def __init__(self):
        self.gain = 0.0

        super().__init__()

    def get_new_node(self, stereo: bool) -> kaudio.BaseNode:
        node = kaudio.VolumeNode(stereo)
        node.gain = self.gain
        return node

    def set_property(self, name, value):
        if name == "gain":
            self.gain = value
            self.node.gain = value
        else:
            super().set_property(name, value)

    def configure_config_widget(self, widget: QWidget):
        super().configure_config_widget(widget)
        self.config_float("gain", "Gain (dB)", self.gain, (-10.0, 10.0), widget)
