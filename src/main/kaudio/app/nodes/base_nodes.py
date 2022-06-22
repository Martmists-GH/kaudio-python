from typing import Any, Optional, List, T, Callable

from NodeGraphQt import BaseNode as GraphBaseNode, Port
from PySide2.QtCore import Qt
from PySide2.QtWidgets import QWidget, QVBoxLayout, QTabWidget, QScrollArea, QHBoxLayout, QLabel, QComboBox
from kaudio.nodes.base import BaseNode as KBaseNode


class BaseNode(GraphBaseNode):
    def __init__(self, stereo: bool, has_widget: bool = False, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.is_stereo = stereo
        self.has_widget = has_widget
        self._tabs = ["config"]
        self.config_widgets = []
        self.mdl = {}
        self.k_node = self.get_node(stereo=stereo)

        for name in self.k_node.inputs():
            self.add_input(name)

        for name in self.k_node.outputs():
            self.add_output(name)

    def process(self):
        self.k_node.process()

    def get_node(self, stereo: bool) -> KBaseNode:
        raise NotImplementedError()

    def add_tab(self, name: str):
        self._tabs.append(name)

    def create_config_tab(self, widget: QWidget):
        for provider in self.config_widgets:
            widget.layout().addWidget(provider())

    def get_config_widget(self) -> Optional[QWidget]:
        if not self.has_widget:
            return None
        root = QTabWidget()
        for name in self._tabs:
            scroll = QScrollArea()
            scroll.setWidgetResizable(True)
            scroll.setVerticalScrollBarPolicy(Qt.ScrollBarAsNeeded)
            widget = QWidget()
            widget.setLayout(QVBoxLayout())
            widget.layout().setAlignment(Qt.AlignTop)
            scroll.setWidget(widget)
            getattr(self, f"create_{name}_tab")(widget)
            root.addTab(widget, name)
        return root

    def config_combo(self, name: str, label: str, options: List[T], default: T, converter: Callable[[T], str] = str):
        def set_func(idx: int):
            self.mdl[name] = idx
            getattr(self, f"set_{name}")(options[idx])

        def create():
            layout = QHBoxLayout()
            lbl = QLabel(label)
            combo = QComboBox()
            for it in options:
                combo.addItem(converter(it), it)
            combo.currentIndexChanged.connect(set_func)
            try:
                if name in self.mdl:
                    i = self.mdl[name]
                else:
                    i = options.index(default)

                combo.setCurrentIndex(i)
                self.mdl[name] = i
            except ValueError:
                combo.setCurrentIndex(0)
                self.mdl[name] = 0
            layout.addWidget(lbl)
            layout.addWidget(combo)
            w = QWidget()
            w.setLayout(layout)
            return w
        self.config_widgets.append(create)

    def on_input_connected(self, in_port: Port, out_port: Port):
        node: BaseNode = out_port.node()
        node.k_node.connect(out_port.name(), self.k_node, in_port.name())

    def on_input_disconnected(self, in_port: Port, out_port: Port):
        node: BaseNode = out_port.node()
        node.k_node.disconnect(out_port.name())


class DualNode(BaseNode):
    def __init__(self, has_widget: bool = False, *args, **kwargs):
        super().__init__(True, has_widget, *args, **kwargs)
        self.add_checkbox("stereo", "Stereo", state=True)

    def set_property(self, name: str, value: Any, push_undo: bool = True):
        super().set_property(name, value, push_undo)
        if name == "stereo":
            for name, port in self.inputs().items():
                port.clear_connections(push_undo=False)

            for name, port in self.outputs().items():
                port.clear_connections(push_undo=False)

            self.k_node = self.get_node(value)


class MonoNode(BaseNode):
    def __init__(self, has_widget: bool = False, *args, **kwargs):
        super().__init__(False, has_widget, *args, **kwargs)


class StereoNode(BaseNode):
    def __init__(self, has_widget: bool = False, *args, **kwargs):
        super().__init__(True, has_widget, *args, **kwargs)
