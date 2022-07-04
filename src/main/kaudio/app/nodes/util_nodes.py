from kaudio.app.nodes.base_nodes import BaseNode
from kaudio.nodes.util import (
    CombinerNode as CombinerNodeImpl,
    SplitterNode as SplitterNodeImpl
)


class CombinerNode(BaseNode[CombinerNodeImpl]):
    __identifier__ = "Utils"
    NODE_NAME = "Combiner"

    def __init__(self):
        super().__init__(False)

    def get_node(self, stereo: bool) -> CombinerNodeImpl:
        return CombinerNodeImpl()


class SplitterNode(BaseNode[SplitterNodeImpl]):
    __identifier__ = "Utils"
    NODE_NAME = "Splitter"

    def __init__(self):
        super().__init__(False)

    def get_node(self, stereo: bool) -> SplitterNodeImpl:
        return SplitterNodeImpl()
