from importlib.metadata import entry_points
from math import floor

from NodeGraphQt import NodeGraph
from PySide2.QtCore import QTimer
from PySide2.QtWidgets import QApplication, QMainWindow

from kaudio.app.widgets.main_widget import MainWidget


class MainApp:
    INSTANCE: 'MainApp' = None

    def __init__(self):
        MainApp.INSTANCE = self

        self.app = QApplication([])
        self.window = QMainWindow()

        graph = NodeGraph()
        graph.set_acyclic(False)

        node_types = []
        for hook in entry_points(group="kaudio.app.nodes"):
            print(f"Loading nodes from {hook.name}")
            func = hook.load()
            added = []
            try:
                func(added)
            except Exception as e:
                print(f"Failed to load nodes from {hook.name}: {e}")
                continue
            node_types.extend(added)

        graph.register_nodes(node_types)

        menu = graph.get_context_menu('graph')
        node_menu = graph.get_context_menu('nodes')

        self.timer = QTimer()
        self.timer.setInterval(floor(1024 / 48000 * 1000) - 10)
        self.timer.timeout.connect(self.run_graph)
        self.timer.start()

        self.window.setCentralWidget(MainWidget(graph))
        self.window.showMaximized()

    def run_graph(self):
        pass

    def run(self):
        self.app.exec_()
