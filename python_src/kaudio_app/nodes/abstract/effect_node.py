import kaudio
import numpy as np
from PySide2.QtWidgets import QWidget
from matplotlib.backends.backend_qtagg import FigureCanvasQTAgg
from matplotlib.figure import Figure

from kaudio_app.nodes.abstract.base_node import BaseNode


class ResponseNode(BaseNode):
    def __init__(self):
        super().__init__()
        self.add_tab_widget("Response")

        self.response_figure = Figure()
        self.response_widget = FigureCanvasQTAgg(self.response_figure)

    def plot_response(self):
        stereo = False
        if isinstance(self.node, kaudio.StereoNode):
            stereo = True

        node = self.get_new_node(stereo)
        node.coeffs_a = self.node.coeffs_a
        node.coeffs_b = self.node.coeffs_b
        input = kaudio.InputNode(stereo)
        output = kaudio.OutputNode(stereo)
        if stereo:
            input.buffer_left = [1] + [0] * 1023
            input.buffer_right = [1] + [0] * 1023
            input.connectStereo(node)
            node.connectStereo(output)
        else:
            input.buffer = [1] + [0] * 1023
            input.connect("output", node, "input")
            node.connect("output", output, "input")
        input.process()
        node.process()
        output.process()
        fft = np.abs(np.fft.fft((output.buffer_left if stereo else output.buffer) + [0] * (48000 - 1024)))
        fft = fft[1:24000]
        ax = self.response_figure.gca()
        ax.clear()
        ax.set_xscale("log")
        ax.set_xlim(1, 24000)
        ax.get_yaxis().set_visible(False)
        ax.plot(fft)
        self.response_widget.draw()

    def configure_response_widget(self, widget: QWidget):
        self.response_figure = Figure()
        self.response_widget = FigureCanvasQTAgg(self.response_figure)
        self.plot_response()
        widget.layout().addWidget(self.response_widget)
