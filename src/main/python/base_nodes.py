import _kaudio
from typing import List


class BaseNode(_kaudio.BaseNode):
    """
    Base class for all nodes.
    """
    def connect(self, output: str, node: 'BaseNode', input: str) -> None:
        """
        Connects the specified output of this node to the input of another node.
        """
        super().connect(output, node, input)

    def disconnect(self, output: str) -> None:
        """
        Disconnects the specified output.
        """
        super().disconnect(output)

    def connect_stereo(self, node: 'BaseNode') -> None:
        """
        Connects this node's output_left and output_right to the specified node's input_left and input_right respectively.
        """
        super().connect_stereo(node)

    def process(self) -> None:
        """
        Run the process function of this node.
        """
        super().process()

    def outputs(self) -> List[str]:
        """
        Returns a list of the outputs of this node.
        """
        return super().outputs()

    def inputs(self) -> List[str]:
        """
        Returns a list of the inputs of this node.
        """
        return super().inputs()


class DualNode(_kaudio.DualNode):
    """
    Base class for nodes that support both mono and stereo usage.
    """
    def __init__(self, stereo: bool):
        super().__init__(stereo)


class MonoNode(_kaudio.MonoNode):
    """
    Base class for nodes that support mono usage.
    """
    pass


class StereoNode(_kaudio.StereoNode):
    """
    Base class for nodes that support stereo usage.
    """
    pass
