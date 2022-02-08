import kaudio
import numpy as np
from PySide2.QtWidgets import QWidget
from pyqtgraph import GraphicsLayoutWidget

from kaudio_app.nodes.abstract.base_node import BaseNode


class ResponseNode(BaseNode):
    def __init__(self, plot_log):
        super().__init__()
        self.plot_log = plot_log
        self.add_tab_widget("Response")

        self.colors = (
            (0, 0, 255),
            (0, 255, 0),
        )

        self.response_widget = GraphicsLayoutWidget()
        self.response_plot = self.response_widget.addPlot()
        self.response_plot_inner = self.response_plot.plot(np.zeros((24000,)), pen=self.colors[1])
        self.setup_response_widget()

    def range(self):
        return 0, 2

    def setup_response_widget(self):
        self.response_widget = GraphicsLayoutWidget()
        self.response_plot = self.response_widget.addPlot()
        self.response_plot.setMenuEnabled(False)
        self.response_plot.setMouseEnabled(False, False)
        self.response_plot.showAxis('bottom', False)
        self.response_plot.showAxis('left', False)
        self.response_plot.setYRange(*self.range())
        self.response_plot.setXRange(1.6, 4.3)
        fig = self.response_plot.plot(np.zeros((24000,)), pen=self.colors[1])
        fig.setLogMode(True, self.plot_log)
        self.response_plot_inner = fig
        self.plot_response()

    def window(self, audio):
        return np.blackman(1024) * audio

    def plot_response(self):
        node = self.get_new_node(False)
        input = kaudio.InputNode(False)
        output = kaudio.OutputNode(False)
        input.buffer = [1] + [0] * 1023
        input.connect("output", node, "input")
        node.connect("output", output, "input")
        input.process()
        node.process()
        output.process()
        fft = np.abs(np.fft.fft(output.buffer + [0] * (48000 - 1024)))
        fft = fft[:24000]
        self.response_plot_inner.setData(fft)

    def configure_response_widget(self, widget: QWidget):
        self.setup_response_widget()
        widget.layout().addWidget(self.response_widget)
