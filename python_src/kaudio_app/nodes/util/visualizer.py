from contextlib import suppress

import kaudio
import numpy as np
from PySide2.QtWidgets import QWidget
from pyqtgraph import GraphicsLayoutWidget, setConfigOptions

from kaudio_app.nodes.abstract.base_node import BaseNode
from kaudio_app.cupy_support import backend, to_backend, from_backend
from kaudio_app.obj_profile import profile_growth

setConfigOptions(antialias=True)


class Visualizer(BaseNode):
    __identifier__ = BaseNode.__identifier__ + ".standard"
    NODE_NAME = "Visualizer"

    def __init__(self):
        super().__init__()

        self.colors = (
            (0, 0, 255),
            (0, 255, 0),
        )
        self.fft_size = 8000 if (backend == np) else 16000  # based gpu

        self.signal_widget = GraphicsLayoutWidget()
        self.signal_plot = self.signal_widget.addPlot()
        self.signal_plots = []
        self.setup_signal_widget()

        self.fft_enabled = 0
        self.fft_data = backend.ndarray(shape=(self.fft_size,), dtype=np.float32)
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
        self.signal_plot.setMenuEnabled(False)
        self.signal_plot.setMouseEnabled(False, False)
        self.signal_plot.showAxis('bottom', False)
        self.signal_plot.showAxis('left', False)
        self.signal_plot.setXRange(0, 1024)
        self.signal_plot.setYRange(-1, 1)
        self.signal_plots = [
            self.signal_plot.plot(np.zeros((1024,)), pen=self.colors[i])
            for i in range(self.stereo + 1)
        ]

    def window(self, audio):
        return np.blackman(1024) * audio

    def setup_fft_widget(self):
        self.fft_widget = GraphicsLayoutWidget()
        self.fft_plot = self.fft_widget.addPlot()
        self.fft_plot.setMenuEnabled(False)
        self.fft_plot.setMouseEnabled(False, False)
        self.fft_plot.showAxis('left', False)
        self.fft_plot.showAxis('bottom', False)
        self.fft_plot.setYRange(0, 0.03, padding=0)
        self.fft_plot.setXRange(0.5, 3.6, padding=0)
        self.fft_plots = []
        for i in range(self.stereo + 1):
            fig = self.fft_plot.plot(np.zeros((self.fft_size,)), pen=self.colors[i])
            fig.setLogMode(True, False)
            self.fft_plots.append(fig)

    def process(self):
        super().process()

        with suppress(RuntimeError):
            if self.signal_widget.isVisible() or self.fft_widget.isVisible():
                if self.stereo:
                    audio = np.array([self.node.buffer_left, self.node.buffer_right])
                else:
                    audio = np.array([self.node.buffer])
                # profile_growth("convert to numpy.ndarray")

                if self.signal_widget.isVisible():
                    self.plot_signal(audio)
                    # profile_growth("plot")

                if self.fft_widget.isVisible():
                    self.fft_enabled = (self.fft_enabled + 1) % 1
                    if self.fft_enabled == 0:

                        self.plot_fft(audio)
                        # profile_growth("plot")

    def plot_signal(self, audio: np.ndarray):
        for i in range(self.stereo + 1):
            self.signal_plots[i].setData(audio[i])

    def plot_fft(self, audio: np.ndarray):
        max_value = np.max(np.abs(audio))
        if max_value == 0:
            max_value = 1
        normalized = audio / max_value

        for i, c in enumerate(normalized):
            self.fft_data[:1024] = to_backend(self.window(c))
            self.fft_data[1024:] = backend.zeros((self.fft_size - 1024,))
            result = backend.abs(backend.fft.rfft(self.fft_data) / self.fft_size)
            as_np = from_backend(result)
            self.fft_plots[i].setData(as_np)

    def configure_signal_widget(self, widget: QWidget):
        self.setup_signal_widget()
        widget.layout().addWidget(self.signal_widget)

    def configure_fft_widget(self, widget: QWidget):
        self.setup_fft_widget()
        widget.layout().addWidget(self.fft_widget)
