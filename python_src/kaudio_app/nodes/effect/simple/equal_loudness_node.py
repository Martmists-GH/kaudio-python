import kaudio

from kaudio_app.nodes.abstract.base_node import BaseNode
from kaudio_app.nodes.abstract.response_node import ResponseNode


class EqualLoudnessNode(ResponseNode):
    __identifier__ = BaseNode.__identifier__ + ".effect.basic"
    NODE_NAME = "Equal Loudness"

    def __init__(self):
        super().__init__(True)

    def get_new_node(self, stereo: bool) -> kaudio.BaseNode:
        return kaudio.EqualLoudnessNode(stereo)
