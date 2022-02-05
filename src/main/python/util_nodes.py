import _kaudio
from typing import List
from kaudio.base_nodes import BaseNode, StereoNode


class InputNode(BaseNode, _kaudio.InputNode):
    buffer: List[float]

    buffer_left: List[float]
    buffer_right: List[float]

    def __init__(self, stereo: bool):
        super(BaseNode, self).__init__(stereo)


class OutputNode(BaseNode, _kaudio.OutputNode):
    buffer: List[float]

    buffer_left: List[float]
    buffer_right: List[float]

    def __init__(self, stereo: bool):
        super(BaseNode, self).__init__(stereo)


class CombinerNode(BaseNode, _kaudio.CombinerNode):
    def __init__(self, stereo: bool):
        super(BaseNode, self).__init__(stereo)


class SplitterNode(BaseNode, _kaudio.SplitterNode):
    def __init__(self, stereo: bool):
        super(BaseNode, self).__init__(stereo)


class StereoSync(StereoNode, _kaudio.StereoSync):
    def __init__(self, node_left: BaseNode, node_right: BaseNode):
        super(StereoNode, self).__init__(node_left, node_right)
