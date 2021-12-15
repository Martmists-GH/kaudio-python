import kaudio

from kaudio_app.nodes.abstract.base_node import BaseNode


class Splitter(BaseNode):
    __identifier__ = BaseNode.__identifier__ + ".standard"
    NODE_NAME = "Splitter"

    def get_new_node(self, stereo: bool) -> kaudio.BaseNode:
        return kaudio.SplitterNode(stereo)
