import NodeGraphQt
import kaudio


class BaseNode(NodeGraphQt.BaseNode):
    __identifier__ = "kaudio"

    def __init__(self):
        self.stereo = True

        super().__init__()
        self.node = self.get_new_node(True)

        if isinstance(self.node, kaudio.DualNode):
            self.add_checkbox("stereo", "stereo", "enabled", True)

        self.set_node(self.node)

    def on_input_connected(self, in_port, out_port):
        # print(f"Connecting {out_port.node().node}:{out_port.name()} to {in_port.node().node}:{in_port.name()}")
        out_port.node().node.connect(out_port.name(), in_port.node().node, in_port.name())

    def on_input_disconnected(self, in_port, out_port):
        # print(f"Disconnecting {out_port.node().node}:{out_port.name()} from {in_port.node().node}:{in_port.name()}")
        out_port.node().node.disconnect(out_port.name())

    def get_new_node(self, stereo: bool) -> kaudio.BaseNode:
        raise NotImplementedError

    def set_node(self, node: kaudio.BaseNode):
        for p in self.input_ports():
            for cp in p.connected_ports():
                cp.disconnect_from(p)

        for p in self.output_ports():
            for cp in p.connected_ports():
                p.disconnect_from(cp)

        for i in self.node.inputs():
            self.delete_input(i)

        for o in self.node.outputs():
            self.delete_output(o)

        for inp in node.inputs():
            self.add_input(inp)

        for inp in node.outputs():
            self.add_output(inp, multi_output=False)

        self.node = node

    def set_property(self, name, value):
        if name == "stereo" and isinstance(self.node, kaudio.DualNode):
            new = self.get_new_node(value)
            self.set_node(new)
            self.stereo = value

    def process(self):
        self.node.process()
