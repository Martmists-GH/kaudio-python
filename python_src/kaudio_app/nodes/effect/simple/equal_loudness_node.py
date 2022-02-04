from PySide2.QtWidgets import QWidget

from kaudio_app.nodes.abstract.base_node import BaseNode
import kaudio


class EqualLoudnessNode(BaseNode):
    __identifier__ = BaseNode.__identifier__ + ".effect.basic"
    NODE_NAME = "Equal Loudness"

    def __init__(self):
        self.gain = 0.0

        super().__init__()

    def get_new_node(self, stereo: bool) -> kaudio.BaseNode:
        node = kaudio.EqualLoudnessNode(stereo)
        return node

    def configure_config_widget(self, widget: QWidget):
        super().configure_config_widget(widget)
