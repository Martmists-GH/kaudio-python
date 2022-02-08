import kaudio
from PySide2.QtWidgets import QWidget

from kaudio_app.nodes.abstract.base_node import BaseNode


class ReverbNode(BaseNode):
    __identifier__ = BaseNode.__identifier__ + ".effect.advanced"
    NODE_NAME = "Reverberation"

    def __init__(self):
        self.room_size = 0
        self.damping = 0
        self.wet = 0
        self.dry = 0.5
        self.width = 0
        super().__init__()

    def get_new_node(self, stereo: bool) -> kaudio.BaseNode:
        node = kaudio.ReverbNode()
        node.room_size = self.room_size
        node.damp = self.damping
        node.wet = self.wet
        node.dry = self.dry
        node.width = self.width
        return node

    def set_property(self, name, value):
        if name == "room_size":
            self.room_size = value
            self.node.room_size = value
        elif name == "damping":
            self.damping = value
            self.node.damp = value
        elif name == "wet":
            self.wet = value
            self.node.wet = value
        elif name == "dry":
            self.dry = value
            self.node.dry = value
        elif name == "width":
            self.width = value
            self.node.width = value
        else:
            super().set_property(name, value)

    def configure_config_widget(self, widget: QWidget):
        super().configure_config_widget(widget)
        self.config_float("room_size", "Room size", self.room_size, (0, 1), widget)
        self.config_float("damping", "Damping", self.damping, (0, 1), widget)
        self.config_float("wet", "Wet signal", self.wet, (0, 1), widget)
        self.config_float("dry", "Dry signal", self.dry, (0, 1), widget)
        self.config_float("width", "Width", self.width, (0, 1), widget)
