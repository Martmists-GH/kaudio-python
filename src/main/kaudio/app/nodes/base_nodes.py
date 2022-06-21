from NodeGraphQt import BaseNode as GraphBaseNode, Port
from PySide2.QtWidgets import QWidget
from kaudio.nodes.base import BaseNode as KBaseNode


class BaseNode(GraphBaseNode):
    def __init__(self, stereo: bool):
        super().__init__()
        self.is_stereo = stereo
        self.k_node = self.get_node(stereo=stereo)

        for name in self.k_node.inputs():
            self.add_input(name)

        for name in self.k_node.outputs():
            self.add_output(name)

    def get_node(self, stereo: bool) -> KBaseNode:
        raise NotImplementedError()

    def get_config_widget(self) -> QWidget:
        raise NotImplementedError()

    def on_input_connected(self, in_port: Port, out_port: Port):
        node: BaseNode = out_port.node()
        node.k_node.connect(out_port.name(), self.k_node, in_port.name())

    def on_input_disconnected(self, in_port: Port, out_port: Port):
        node: BaseNode = out_port.node()
        node.k_node.disconnect(out_port.name())


class DualNode(BaseNode):
    def __init__(self):
        # Add stereo toggle
        super().__init__(True)

    def get_node(self, stereo: bool) -> KBaseNode:
        raise NotImplementedError()


class MonoNode(BaseNode):
    def __init__(self):
        super().__init__(False)

    def get_node(self, stereo: bool) -> KBaseNode:
        raise NotImplementedError()


class StereoNode(BaseNode):
    def __init__(self):
        super().__init__(True)

    def get_node(self, stereo: bool) -> KBaseNode:
        raise NotImplementedError()
