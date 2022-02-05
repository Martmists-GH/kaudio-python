import kaudio
from PySide2.QtWidgets import QWidget

from kaudio_app.nodes.abstract.base_node import BaseNode


class Bs2bNode(BaseNode):
    __identifier__ = BaseNode.__identifier__ + ".effect.basic"
    NODE_NAME = "BS2B"

    def __init__(self):
        self.frequency = 700
        self.level = 45
        super().__init__()

    def get_new_node(self, stereo: bool) -> kaudio.BaseNode:
        node = kaudio.Bs2bNode()
        node.frequency = self.frequency
        node.feed = self.level
        return node

    def set_property(self, name, value):
        if name == "frequency":
            self.frequency = value
            self.node.frequency = value
        elif name == "crossfeed":
            self.level = min(max(round(value * 10), 10), 150)
            self.node.feed = self.level
        else:
            super().set_property(name, value)

    def configure_config_widget(self, widget: QWidget):
        super().configure_config_widget(widget)
        self.config_int("frequency", "Frequency", self.frequency, (300, 2000), widget)
        self.config_float("crossfeed", "Crossfeed (dB)", self.level / 10, (1.0, 15.0), widget)
