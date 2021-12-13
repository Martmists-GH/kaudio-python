from kaudio_app.nodes.abstract.base_node import BaseNode
import kaudio


class VolumeNode(BaseNode):
    NODE_NAME = "Volume"

    def __init__(self):
        self.gain = 0.0

        super().__init__()

        self.add_float_input("gain", "gain (dB)", 0.0, (-10.0, 10.0))

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
