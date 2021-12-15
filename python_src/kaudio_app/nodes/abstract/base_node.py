from typing import Tuple

import NodeGraphQt
import kaudio
from PySide2.QtCore import Qt
from PySide2.QtWidgets import QWidget, QScrollArea, QTabWidget, QVBoxLayout, QCheckBox, QComboBox

from kaudio_app.ui.props import IntSlider, ComboBox, FloatSlider


class BaseNode(NodeGraphQt.BaseNode):
    __identifier__ = "kaudio"

    def __init__(self):
        self.stereo = True

        super().__init__()
        self.node = self.get_new_node(True)

        self.set_node(self.node)

        self.tabs = {}
        self.add_tab_widget("Config")

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
        if name == "stereo" and isinstance(self.node, (kaudio.DualNode, kaudio.SplitterNode, kaudio.CombinerNode)):
            new = self.get_new_node(value)
            self.set_node(new)
            self.stereo = value

    def process(self):
        self.node.process()

    def config_checkbox(self, name: str, label: str, value: bool, widget: QWidget):
        checkbox = QCheckBox(label)
        checkbox.setChecked(value)
        checkbox.stateChanged.connect(lambda *args, **kwargs: self.set_property(name, checkbox.isChecked()))
        widget.layout().addWidget(checkbox)

    def config_int(self, name: str, label: str, value: int, range: Tuple[int, int], widget: QWidget):
        _widget = IntSlider(label, value, range)
        _widget.on_set.connect(lambda new: self.set_property(name, new))
        widget.layout().addWidget(_widget)

    def config_float(self, name: str, label: str, value: float, range: Tuple[float, float], widget: QWidget):
        _widget = FloatSlider(label, value, range)
        _widget.on_set.connect(lambda new: self.set_property(name, new))
        widget.layout().addWidget(_widget)
        
    def config_combobox(self, name: str, label: str, options: list, widget: QWidget):
        combo = ComboBox(label, options)
        combo.on_set.connect(lambda new: self.set_property(name, new))
        widget.layout().addWidget(combo)

    def configure_config_widget(self, widget: QWidget):
        if isinstance(self.node, (kaudio.DualNode, kaudio.SplitterNode, kaudio.CombinerNode)):
            self.config_checkbox("stereo", "Stereo", self.stereo, widget)

    def add_tab_widget(self, name: str):
        def get():
            scroll = QScrollArea()
            scroll.setWidgetResizable(True)
            scroll.setLayout(QVBoxLayout())
            scroll.layout().setAlignment(Qt.AlignTop)
            scroll.setVerticalScrollBarPolicy(Qt.ScrollBarAsNeeded)
            getattr(self, f"configure_{name.lower()}_widget")(scroll)
            return scroll
        self.tabs[name] = get

    def set_config_widget(self, widget: QTabWidget):
        for name, box in self.tabs.items():
            widget.addTab(box(), name)
