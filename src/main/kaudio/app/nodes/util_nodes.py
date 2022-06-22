from kaudio.app.nodes.base_nodes import BaseNode
from kaudio.nodes.base import BaseNode as KBaseNode
from kaudio.nodes.util import (
    CombinerNode as CombinerNodeImpl,
    SplitterNode as SplitterNodeImpl
)


class CombinerNode(BaseNode):
    __identifier__ = "Utils"
    NODE_NAME = "Combiner"

    def __init__(self):
        super().__init__(False)

    def get_node(self, stereo: bool) -> KBaseNode:
        return CombinerNodeImpl()


class SplitterNode(BaseNode):
    __identifier__ = "Utils"
    NODE_NAME = "Splitter"

    def __init__(self):
        super().__init__(False)

    def get_node(self, stereo: bool) -> KBaseNode:
        return SplitterNodeImpl()
