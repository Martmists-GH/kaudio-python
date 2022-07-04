from math import log10
from typing import Any, Optional, List, T, Callable, Tuple, TypeVar, Generic

import numpy as np
from NodeGraphQt import BaseNode as GraphBaseNode, Port
from PySide2.QtCore import Qt
from PySide2.QtWidgets import QWidget, QVBoxLayout, QTabWidget, QScrollArea, QHBoxLayout, QLabel, QComboBox, QSpinBox, \
    QDoubleSpinBox

from kaudio.app.utils.cava_fft import CavaFFT
from kaudio.app.utils.cupy_support import backend, to_backend, from_backend
from kaudio.nodes.base import BaseNode as KBaseNode, StereoNode as KStereoNode
from kaudio.nodes.util import InputNode, OutputNode
from pyqtgraph import GraphicsLayoutWidget, BarGraphItem
from scipy.signal.windows import blackmanharris

N = TypeVar("N", bound=KBaseNode, covariant=True)


class BaseNode(GraphBaseNode, Generic[N]):
    def __init__(self, stereo: bool, config: dict = None, has_widget: bool = False, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.is_stereo = stereo
        self.config = config or {}
        self.has_widget = has_widget
        self.k_node = self.get_new_node(stereo=stereo)

        # Settings menu
        self._tabs = ["config"]
        self.config_widgets = []
        self.mdl = {}

        # Plotting
        self.plot_data = [0]*1024
        self.plot_update_listener = lambda: None

        # Ports
        for name in self.k_node.inputs():
            self.add_input(name)

        for name in self.k_node.outputs():
            self.add_output(name)

    def process(self):
        self.k_node.process()

    def get_new_node(self, stereo: bool) -> N:
        node = self.get_node(stereo)
        for k, v in self.config.items():
            setattr(node, k, v)
        return node

    def get_node(self, stereo: bool) -> N:
        raise NotImplementedError()

    def add_tab(self, name: str):
        self._tabs.append(name)

    def create_config_tab(self, widget: QWidget):
        for provider in self.config_widgets:
            widget.layout().addWidget(provider())

    def window(self, in_data: np.ndarray) -> np.ndarray:
        win = blackmanharris(in_data.shape[0])
        return in_data * win

    def create_frequencies_tab(self, widget: QWidget):
        num_bars = 160
        cava = CavaFFT(num_bars, 1, True, 0.77, 50, 10_000)

        response_widget = GraphicsLayoutWidget()
        response_plot = response_widget.addPlot()
        bar_chart = BarGraphItem(x=np.arange(num_bars), height=np.zeros(num_bars), width=0.5, brush='r')
        response_plot.showAxis('bottom', False)
        response_plot.showAxis('left', False)
        response_plot.setYRange(0, 1)
        response_plot.addItem(bar_chart)
        response_plot.setMouseEnabled(False, False)
        response_plot.setMenuEnabled(False)

        def update_plot():
            bar_data = cava.execute(np.asarray(self.plot_data).reshape(1024, 1))
            bar_chart.setOpts(height=bar_data)

        update_plot()

        self.plot_update_listener = update_plot

        def custom_del(_self):
            self.plot_update_listener = lambda: None
        response_widget.__del__ = custom_del

        widget.layout().addWidget(response_widget)

    def create_response_tab(self, widget: QWidget):
        num_bars = 200
        fft_size = 48000
        start = 20
        n = int(fft_size//2)

        response_widget = GraphicsLayoutWidget()
        response_plot = response_widget.addPlot()
        bar_chart = BarGraphItem(x=np.arange(num_bars), height=np.zeros((num_bars,)), width=0.5, brush='r')
        response_plot.showAxis('bottom', False)
        response_plot.showAxis('left', False)
        response_plot.setYRange(-3, 3)
        response_plot.setXRange(0, num_bars)
        response_plot.addItem(bar_chart)
        response_plot.setMouseEnabled(False, False)
        response_plot.setMenuEnabled(False)
        response_plot.hideButtons()

        def update_plot():
            data = np.asarray(self.plot_data)
            backend_arr = to_backend(np.append(data, np.zeros((fft_size - 1024,)), 0))
            fft_data = from_backend(backend.log(backend.abs(backend.fft.fft(backend_arr)[:n])))
            bar_data = []
            for i in np.geomspace(start, n, num_bars):
                new = int(i)
                bar_data.append(fft_data[min(new, n-1)])
            bar_chart.setOpts(height=bar_data)

        update_plot()

        self.plot_update_listener = update_plot

        def custom_del(_self):
            self.plot_update_listener = lambda: None
        response_widget.__del__ = custom_del

        widget.layout().addWidget(response_widget)

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
            root.addTab(widget, name.capitalize())
        return root

    def config_combo(self, name: str, label: str,
                     options: List[T] | Callable[[], List[T]],
                     default: T, converter: Callable[[T], str] = str):
        def set_func(o: List[T], idx: int):
            self.mdl[name] = idx
            getattr(self, f"set_{name}")(o[idx])

        def create():
            layout = QHBoxLayout()
            lbl = QLabel(label)
            combo = QComboBox()
            opts = options() if callable(options) else options
            for it in opts:
                combo.addItem(converter(it), it)
            combo.currentIndexChanged.connect(lambda idx: set_func(opts, idx))
            try:
                if name in self.mdl:
                    i = self.mdl[name]
                else:
                    i = opts.index(default)

                combo.setCurrentIndex(i)
                self.mdl[name] = i
            except ValueError:
                combo.setCurrentText("")
            layout.addWidget(lbl)
            layout.addWidget(combo)
            w = QWidget()
            w.setLayout(layout)
            return w
        self.config_widgets.append(create)

    def config_int(self, name: str, label: str, default: int, value_range: Tuple[int, int, int]):
        def set_func(val: int):
            self.mdl[name] = val
            getattr(self, f"set_{name}")(val)

        def create():
            layout = QHBoxLayout()
            lbl = QLabel(label)
            spin = QSpinBox()
            spin.setRange(value_range[0], value_range[1])
            spin.setSingleStep(value_range[2])
            spin.setValue(self.mdl.get(name) or default)
            spin.valueChanged.connect(set_func)
            layout.addWidget(lbl)
            layout.addWidget(spin)
            w = QWidget()
            w.setLayout(layout)
            return w

        self.config_widgets.append(create)

    def config_float(self, name: str, label: str, default: float, value_range: Tuple[float, float, float]):
        def set_func(val: float):
            self.mdl[name] = val
            getattr(self, f"set_{name}")(val)

        def create():
            layout = QHBoxLayout()
            lbl = QLabel(label)
            spin = QDoubleSpinBox()
            spin.setRange(value_range[0], value_range[1])
            spin.setSingleStep(value_range[2])
            spin.setValue(self.mdl.get(name) or default)
            spin.valueChanged.connect(set_func)
            layout.addWidget(lbl)
            layout.addWidget(spin)
            w = QWidget()
            w.setLayout(layout)
            return w

        self.config_widgets.append(create)

    def update_plot(self):
        tmp = self.get_new_node(False)
        stereo = isinstance(tmp, KStereoNode)
        inp = InputNode(stereo)
        out = OutputNode(stereo)

        if stereo:
            inp.connect("outputLeft", tmp, "inputLeft")
            inp.connect("outputRight", tmp, "inputRight")
            tmp.connect("outputLeft", out, "inputLeft")
            tmp.connect("outputRight", out, "inputRight")
            inp.bufLeft = [1] + [0] * 1023
            inp.bufRight = [0] * 1023 + [1]
        else:
            inp.connect("output", tmp, "input")
            tmp.connect("output", out, "input")
            inp.buffer = [1] + [0] * 1023

        inp.process()
        tmp.process()
        out.process()

        if stereo:
            self.plot_data = out.bufLeft
        else:
            self.plot_data = out.buffer

        try:
            self.plot_update_listener()
        except RuntimeError:
            self.plot_update_listener = lambda: None

    def on_input_connected(self, in_port: Port, out_port: Port):
        node: BaseNode = out_port.node()
        node.k_node.connect(out_port.name(), self.k_node, in_port.name())

    def on_input_disconnected(self, in_port: Port, out_port: Port):
        node: BaseNode = out_port.node()
        node.k_node.disconnect(out_port.name())


class DualNode(BaseNode[N], Generic[N]):
    def __init__(self, config: dict = None, has_widget: bool = False, *args, **kwargs):
        super().__init__(True, config, has_widget, *args, **kwargs)
        self.set_port_deletion_allowed(True)
        self.add_checkbox("stereo", text="Stereo", state=True)

    def set_property(self, name: str, value: Any, push_undo: bool = True):
        super().set_property(name, value, push_undo)
        if name == "stereo":
            for name, port in self.inputs().items():
                port.clear_connections(push_undo=False)

            for name, port in self.outputs().items():
                port.clear_connections(push_undo=False)

            for i in self.inputs():
                self.delete_input(i)
            for o in self.outputs():
                self.delete_output(o)

            self.k_node = self.get_new_node(value)

            for name in self.k_node.inputs():
                self.add_input(name)

            for name in self.k_node.outputs():
                self.add_output(name)


class MonoNode(BaseNode[N], Generic[N]):
    def __init__(self, config: dict = None, has_widget: bool = False, *args, **kwargs):
        super().__init__(False, config, has_widget, *args, **kwargs)


class StereoNode(BaseNode[N], Generic[N]):
    def __init__(self, config: dict = None, has_widget: bool = False, *args, **kwargs):
        super().__init__(True, config, has_widget, *args, **kwargs)
