import _kaudio
from typing import List


class BaseNode(_kaudio.BaseNode):
    """
    Base class for all nodes.
    """
    def connect(self, output: str, node: 'BaseNode', input: str) -> None:
        """
        Connects the output of this node to the input of another node.
        """
        super().connect(output, node, input)

    def disconnect(self, output: str) -> None:

        super().disconnect(output)

    def connect_stereo(self, node: 'BaseNode') -> None:
        super().connect_stereo(node)

    def process(self) -> None:
        super().process()

    def outputs(self) -> List[str]:
        return super().outputs()

    def inputs(self) -> List[str]:
        return super().inputs()


class DualNode(_kaudio.DualNode):
    def __init__(self, stereo: bool):
        super().__init__(stereo)


class MonoNode(_kaudio.MonoNode):
    pass


class StereoNode(_kaudio.StereoNode):
    pass
