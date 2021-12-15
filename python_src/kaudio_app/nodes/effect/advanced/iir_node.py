import kaudio
from PySide2.QtWidgets import QWidget

from kaudio_app.nodes.abstract.base_node import BaseNode
from kaudio_app.nodes.abstract.effect_node import ResponseNode
from kaudio_app.ui.iir import IIRWidget


class IIRNode(ResponseNode):
    __identifier__ = BaseNode.__identifier__ + ".effect.advanced"
    NODE_NAME = "N-Order IIR"

    def __init__(self):
        self.order = 2
        super().__init__()
        self.iir_config_widget = IIRWidget(self.node, "IIR")

        self.add_tab_widget("Response")

    def set_iir(self, value, index):
        coeffs = self.node.coeffs_a + self.node.coeffs_b
        coeffs[index] = value
        self.node.coeffs_a = coeffs[:self.order+1]
        self.node.coeffs_b = coeffs[self.order+1:]
        self.plot_response()

    def get_new_node(self, stereo: bool) -> kaudio.BaseNode:
        return kaudio.IIRNode(self.order, stereo)

    def set_property(self, name, value):
        if name == "order" and value != self.order:
            self.order = int(value)
            node = kaudio.IIRNode(self.order, self.stereo)
            self.set_node(node)
            self.iir_config_widget.set_node(node)
            self.iir_config_widget.set_order(int(value))
        else:
            super().set_property(name, value)
            if name == "stereo":
                self.iir_config_widget.set_node(self.node)

    def configure_config_widget(self, widget: QWidget):
        super().configure_config_widget(widget)
        self.config_int("order", "Order", self.order, (1, 10), widget)
        self.iir_config_widget = IIRWidget(self.node, "IIR")
        self.iir_config_widget.set_order(self.order)
        self.iir_config_widget.set_value.connect(self.set_iir)
        widget.layout().addWidget(self.iir_config_widget)
