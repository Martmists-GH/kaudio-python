from contextlib import suppress

import kaudio
import numpy as np
import scipy.fft
from PySide2.QtWidgets import QWidget
from matplotlib.backends.backend_qtagg import FigureCanvasQTAgg
from matplotlib.figure import Figure
from pyqtgraph import PlotWidget, GraphicsLayoutWidget

from kaudio_app.nodes.abstract.base_node import BaseNode


class Visualizer(BaseNode):
    __identifier__ = BaseNode.__identifier__ + ".standard"
    NODE_NAME = "Visualizer"

    def __init__(self):
        super().__init__()

        self.colors = (
            (0, 0, 255),
            (0, 255, 0),
        )
        self.fft_size = 8000

        self.signal_widget = GraphicsLayoutWidget()
        self.signal_plot = self.signal_widget.addPlot()
        self.signal_plots = []
        self.setup_signal_widget()

        self.fft_widget = GraphicsLayoutWidget()
        self.fft_plot = self.fft_widget.addPlot()
        self.fft_plots = []
        self.setup_fft_widget()

        self.add_tab_widget("Signal")
        self.add_tab_widget("FFT")

    def get_new_node(self, stereo: bool) -> kaudio.BaseNode:
        return kaudio.OutputNode(stereo)

    def set_property(self, name, value):
        super().set_property(name, value)
        if name == "stereo":
            self.setup_signal_widget()
            self.setup_fft_widget()

    def setup_signal_widget(self):
        self.signal_widget = GraphicsLayoutWidget()
        self.signal_plot = self.signal_widget.addPlot()
        self.signal_plot.enableAutoRange('xy', False)
        self.signal_plot.showAxis('bottom', False)
        self.signal_plot.showAxis('left', False)
        self.signal_plot.setXRange(0, 1024)
        self.signal_plot.setYRange(-1, 1)
        self.signal_plots = [
            self.signal_plot.plot(np.zeros((1024,)), pen=self.colors[i])
            for i in range(self.stereo + 1)
        ]

    def window(self, audio: list):
        return list(np.bartlett(1024) * audio)


    def setup_fft_widget(self):
        self.fft_widget = GraphicsLayoutWidget()
        self.fft_plot = self.fft_widget.addPlot()

        self.fft_plot.showAxis('bottom', False)
        self.fft_plot.setXRange(0, int(self.fft_size / 2))
        self.fft_plot.setYRange(0, 1)
        self.fft_plot.setLogMode(False, False)
        self.fft_plot.enableAutoRange('xy', False)
        self.fft_plots = [
            self.fft_plot.plot(np.logspace(1, self.fft_size / 2, num=int(self.fft_size / 2)), np.zeros((int(self.fft_size / 2),)), pen=self.colors[i])
            for i in range(self.stereo + 1)
        ]

    def process(self):
        super().process()

        with suppress(RuntimeError):
            if self.signal_widget.isVisible() or self.fft_widget.isVisible():
                if self.stereo:
                    audio = np.array([self.node.buffer_left, self.node.buffer_right])
                else:
                    audio = np.array([self.node.buffer])

                if self.signal_widget.isVisible():
                    self.plot_signal(audio)

                if self.fft_widget.isVisible():
                    self.plot_fft(audio)

    def plot_signal(self, audio: np.ndarray):
        for i, channel in enumerate(audio):
            self.signal_plots[i].setData(channel)

    def plot_fft(self, audio: np.ndarray):
        for i, c in enumerate(audio):
            fft = np.abs(scipy.fft.rfft(self.window(c) + [0] * (self.fft_size - 1024), axis=0))
            self.fft_plots[i].setData(fft)

    def configure_signal_widget(self, widget: QWidget):
        self.setup_signal_widget()
        widget.layout().addWidget(self.signal_widget)

    def configure_fft_widget(self, widget: QWidget):
        self.setup_fft_widget()
        widget.layout().addWidget(self.fft_widget)
