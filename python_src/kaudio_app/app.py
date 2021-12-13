from math import floor
from threading import Lock

from NodeGraphQt import NodeGraph, setup_context_menu
from PySide2 import QtWidgets, QtCore

from kaudio_app.nodes.util.audio_input import AudioInput
from kaudio_app.nodes.util.audio_output import AudioOutput
from kaudio_app.nodes.effect.volume_node import VolumeNode


class App:
    INSTANCE = None

    def __init__(self):
        App.INSTANCE = self

        self.app = QtWidgets.QApplication([])
        self.graph = NodeGraph()
        self.lock = Lock()

        setup_context_menu(self.graph)
        self.menu = self.graph.get_context_menu('graph')

        def show_menu(graph):
            self.graph._viewer.tab_search_set_nodes(self.graph._node_factory.names)
            self.graph._viewer.tab_search_toggle()

        self.menu.add_command("Add node", show_menu)

        graph_widget = self.graph.widget
        graph_widget.resize(1100, 800)
        graph_widget.show()

        self.graph.register_nodes([
            AudioInput,
            AudioOutput,
            VolumeNode,
        ])

        self.graph.auto_layout_nodes()
        self.graph.fit_to_selection()

        self.timer = QtCore.QTimer()
        self.timer.setInterval(floor(1024 / 48000 * 1000)-10)
        self.timer.timeout.connect(self.run_graph)
        self.timer.start()

    def nodes_ordered(self):
        visited = set()

        def visit(node):
            visited.add(node)
            parents = node.connected_input_nodes().values()
            children = node.connected_output_nodes().values()
            return [
                *[j for el in parents for n in el if n not in visited for j in visit(n)],
                node,
                *[j for el in children for n in el if n not in visited for j in visit(n)]
            ]

        sorted_nodes = [n for node in self.graph.all_nodes() if node not in visited for n in visit(node)]
        return sorted_nodes

    def run_graph(self):
        with self.lock:
            nodes = self.graph.all_nodes()
            if nodes:
                for n in self.nodes_ordered():
                    n.process()

    def run(self):
        self.app.exec_()
