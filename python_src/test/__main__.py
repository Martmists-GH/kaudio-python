from NodeGraphQt import NodeGraph, BaseNode
from PySide2.QtCore import QCoreApplication, Qt

from math import floor

import numpy as np
from PySide2.QtCore import QTimer
from PySide2.QtWidgets import QTabWidget, QApplication, QMainWindow, QWidget, QVBoxLayout, QLabel, QHBoxLayout
from pyqtgraph import GraphicsLayoutWidget


class MyNode(BaseNode):
    __identifier__ = "dummy"
    NODE_NAME = "My Node"

    def __init__(self):
        super(MyNode, self).__init__()
        self.widget = MyWidget()

    def process(self):
        self.widget.process()

    def getWidget(self):
        self.widget = MyWidget()
        return self.widget


class MyWidget(QTabWidget):
    def __init__(self):
        super().__init__()
        self.setLayout(QVBoxLayout())
        self.addTab(self.make_dummy(), "Dummy")
        self.addTab(self.make_plot(), "Plot")

    def process(self):
        if self.signal_widget.isVisible():
            audio = np.random.normal(size=(2, 1024))
            for i, c in enumerate(audio):
                self.signal_plots[i].setData(c)

    def make_dummy(self):
        widget = QWidget()
        layout = QVBoxLayout()
        layout.addWidget(QLabel("Dummy"))
        widget.setLayout(layout)
        return widget

    def make_plot(self):
        widget = QWidget()
        layout = QVBoxLayout()

        self.signal_widget = GraphicsLayoutWidget()
        self.signal_plot = self.signal_widget.addPlot()
        self.signal_plot.setMenuEnabled(False)
        self.signal_plot.showAxis('bottom', False)
        self.signal_plot.showAxis('left', False)
        self.signal_plot.setXRange(0, 1024)
        self.signal_plot.setYRange(-1, 1)
        self.signal_plots = [
            self.signal_plot.plot(np.zeros((1024,))) for _ in range(2)
        ]

        layout.addWidget(self.signal_widget)
        widget.setLayout(layout)
        return widget


class App:
    INSTANCE = None

    def __init__(self):
        self.app = QApplication([])
        self.window = QMainWindow()
        self.main_widget = QWidget()

        self.graph = NodeGraph()
        self.graph.register_nodes([MyNode])
        self.graph.node_selection_changed.connect(self.on_selection_change)

        layout = QHBoxLayout()
        layout.addWidget(self.graph.widget, 10)

        self.main_widget = QWidget()
        self.main_widget.setLayout(layout)

        self.window.setCentralWidget(self.main_widget)

        self.timer = QTimer()
        self.timer.setInterval(floor(1024 / 48000 * 1000) - 10)
        self.timer.timeout.connect(self.process)
        self.timer.start()

        self.window.showMaximized()

    def on_selection_change(self, added, removed):
        for n in added:
            widget = n.getWidget()
            widget.setWindowTitle("Node Properties")
            widget.show()

    def process(self):
        for n in self.graph.all_nodes():
            n.process()

    def run(self):
        self.app.exec_()


def main():
    QCoreApplication.setAttribute(Qt.AA_EnableHighDpiScaling)
    # QApplication.setHighDpiScaleFactorRoundingPolicy(Qt.HighDpiScaleFactorRoundingPolicy.PassThrough)
    app = App()
    app.run()


if __name__ == "__main__":
    main()
