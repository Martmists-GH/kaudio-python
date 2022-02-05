import kaudio

from kaudio_app.nodes.abstract.base_node import BaseNode


class EqualLoudnessNode(BaseNode):
    __identifier__ = BaseNode.__identifier__ + '.equal_loudness'
    NODE_NAME = 'Equal Loudness'

    def __init__(self):
        super().__init__()

    def get_new_node(self, stereo: bool) -> kaudio.BaseNode:
        return kaudio.EqualLoudnessNode(stereo)
