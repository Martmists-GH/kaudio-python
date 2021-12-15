import kaudio
import numpy as np
from PySide2.QtWidgets import QWidget
from matplotlib.backends.backend_qtagg import FigureCanvasQTAgg
from matplotlib.figure import Figure
from vispy.plot import PlotWidget
from vispy.scene import SceneCanvas

from kaudio_app.nodes.abstract.base_node import BaseNode

x_signal = np.arange(0, 1024)
x_fft = np.arange(0, 24000)


class Visualizer(BaseNode):
    __identifier__ = BaseNode.__identifier__ + ".standard"
    NODE_NAME = "Visualizer"

    def __init__(self):
        super().__init__()

        # TODO: Find a good plotting library
        self.signal_figure = PlotWidget()
        self.signal_canvas = SceneCanvas()
        self.signal_widget = self.signal_canvas.native

        self.fft_figure = Figure()
        self.fft_widget = FigureCanvasQTAgg(self.fft_figure)

        self.add_tab_widget("Signal")
        self.add_tab_widget("FFT")

    def get_new_node(self, stereo: bool) -> kaudio.BaseNode:
        return kaudio.OutputNode(stereo)

    def process(self):
        super().process()

        if self.signal_widget.isVisible() or self.fft_widget.isVisible():
            if self.stereo:
                audio = np.array([self.node.buffer_left, self.node.buffer_right])
            else:
                audio = np.array([self.node.buffer])

            if self.signal_widget.isVisible():
                self.plot_signal(audio)
            if self.fft_widget.isVisible():
                print("Plotting fft")
                self.plot_fft(audio)
                print("Plotting fft done")

    def plot_signal(self, audio: np.ndarray):
        for channel in audio:
            self.signal_figure._width_limits
            self.signal_figure.plot(x_signal, channel)

    def plot_fft(self, audio: np.ndarray):
        ax = self.signal_figure.gca()
        lines = ax.get_lines()
        num_channels = self.stereo + 1
        if len(lines) != num_channels:
            zeros = np.zeros((24000,))
            ax.clear()
            ax.plot([x_fft] * num_channels, [zeros] * num_channels)
            lines = ax.get_lines()

        for l, c in zip(lines, audio):
            fft = np.abs(np.fft.fft(list(c) + [0] * (48000 - 1024))[:24000])
            l.set_data([x_fft, fft])

    def configure_signal_widget(self, widget: QWidget):
        self.signal_figure = PlotWidget()
        self.signal_canvas = SceneCanvas()
        self.signal_widget = self.signal_canvas.native
        widget.layout().addWidget(self.signal_widget)

    def configure_fft_widget(self, widget: QWidget):
        self.fft_figure = Figure()
        self.fft_widget = FigureCanvasQTAgg(self.fft_figure)
        widget.layout().addWidget(self.fft_widget)
