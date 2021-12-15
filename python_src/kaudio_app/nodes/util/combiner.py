import kaudio

from kaudio_app.nodes.abstract.base_node import BaseNode


class Combiner(BaseNode):
    __identifier__ = BaseNode.__identifier__ + ".standard"
    NODE_NAME = "Combiner"

    def get_new_node(self, stereo: bool) -> kaudio.BaseNode:
        return kaudio.CombinerNode(stereo)
