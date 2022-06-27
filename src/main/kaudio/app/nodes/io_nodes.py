from time import sleep

import numpy as np
from PySide2.QtWidgets import QWidget
from kaudio.app.nodes.base_nodes import StereoNode
from kaudio.app.utils.sounddevice_handler import device_map, open_stream
from kaudio.nodes.base import BaseNode as KBaseNode
from kaudio.nodes.util import (
    InputNode as InputNodeImpl,
    OutputNode as OutputNodeImpl
)
from pyqtgraph import GraphicsLayoutWidget, BarGraphItem


class InputNode(StereoNode):
    __identifier__ = "Devices"
    NODE_NAME = "Input Device"

    def __init__(self):
        super().__init__(has_widget=True)
        self.stream = None
        self.config_combo("device",
                          "Input Device",
                          [({"name": "-"}, -1)] + list(filter(lambda x: x[0]['max_input_channels'] >= 2, device_map().values())),
                          "-",
                          lambda it: it[0]["name"])

    def __del__(self):
        if self.stream is not None:
            self.stream.stop()
            self.stream.close()
            self.stream = None

    def set_device(self, value):
        stream_idx = value[1]
        if self.stream is not None:
            stream = self.stream
            self.stream = None
            stream.stop()
            stream.close()

        if stream_idx < 0:
            return

        stream = open_stream(
            stream_idx,
            True,
            True
        )
        stream.start()
        self.stream = stream

    def process(self):
        if self.stream is None:
            self.k_node.bufLeft = [0] * 1024
            self.k_node.bufRight = [0] * 1024
        else:
            while self.stream.read_available < 1024 * 2:
                sleep(0.0001)
            arr, overflowed = self.stream.read(1024)
            if overflowed:
                print("Overflowed")
                print(self.stream.read_available)
            self.k_node.bufLeft = list(arr[:1024, 0])
            self.k_node.bufRight = list(arr[:1024, 1])
        super().process()

    def get_node(self, stereo: bool) -> KBaseNode:
        return InputNodeImpl(True)


class OutputNode(StereoNode):
    __identifier__ = "Devices"
    NODE_NAME = "Output Device"

    def __init__(self):
        super().__init__(has_widget=True)
        self.stream = None
        self.device = ({}, -1)
        self.add_tab("signal")
        self.add_tab("frequencies")
        self.config_combo("device",
                          "Output Device",
                          [({"name": "-"}, -1)] + list(filter(lambda x: x[0]['max_output_channels'] >= 2, device_map().values())),
                          "-",
                          lambda it: it[0]["name"])
        self.plot_signal_listener = lambda: None

    def update_plot(self):
        left = np.asarray(self.k_node.bufLeft)
        right = np.asarray(self.k_node.bufRight)
        self.plot_data = list((left + right) / 2)
        self.plot_update_listener()
        self.plot_signal_listener()

    def create_signal_tab(self, widget: QWidget):
        size = min(int(48000 * 0.05), len(self.plot_data))
        num_bars = size

        response_widget = GraphicsLayoutWidget()
        response_plot = response_widget.addPlot()
        bar_chart = BarGraphItem(x=np.arange(num_bars), height=np.zeros((num_bars,)), width=0.5, brush='r')
        response_plot.showAxis('bottom', False)
        response_plot.showAxis('left', False)
        response_plot.setYRange(-1, 1)
        response_plot.setXRange(0, num_bars)
        response_plot.addItem(bar_chart)
        response_plot.setMouseEnabled(False, False)
        response_plot.setMenuEnabled(False)
        data = np.zeros((size,))

        def update_plot():
            nonlocal data
            data = np.append(data[len(self.plot_data):], np.asarray(self.plot_data), 0)
            bar_chart.setOpts(height=data)

        update_plot()

        self.plot_signal_listener = update_plot

        def custom_del(_self):
            self.plot_signal_listener = lambda: None
        response_widget.__del__ = custom_del

        widget.layout().addWidget(response_widget)

    def __del__(self):
        if self.stream is not None:
            self.stream.stop()
            self.stream.close()
            self.stream = None

    def set_device(self, value):
        self.device = value
        stream_idx = value[1]
        if self.stream is not None:
            stream = self.stream
            self.stream = None
            stream.stop()
            stream.close()

        if stream_idx < 0:
            return

        stream = open_stream(
            stream_idx,
            True,
            False
        )
        stream.start()
        self.stream = stream

    def process(self):
        super().process()
        self.update_plot()

        if self.stream is None:
            return

        arr = np.zeros((1024, 2), dtype=np.float32)
        arr[:1024, 0] = self.k_node.bufLeft
        arr[:1024, 1] = self.k_node.bufRight

        while self.stream.write_available < 1024:
            sleep(0.0001)

        if self.stream.write(arr):
            print("Underflowed")
            print(self.stream.write_available)

    def get_node(self, stereo: bool) -> KBaseNode:
        return OutputNodeImpl(True)
