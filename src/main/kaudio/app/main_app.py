from importlib.metadata import entry_points
from math import floor
from typing import List

from NodeGraphQt import NodeGraph
from PySide2.QtCore import QTimer
from PySide2.QtWidgets import QApplication, QMainWindow
from kaudio.app.nodes.base_nodes import BaseNode

from kaudio.app.widgets.main_widget import MainWidget


class MainApp:
    INSTANCE: 'MainApp' = None

    def __init__(self):
        MainApp.INSTANCE = self

        self.app = QApplication([])
        self.window = QMainWindow()

        self.graph = graph = NodeGraph()
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
            print(f"Loaded {len(added)} nodes from {hook.name}")
            node_types.extend(added)

        graph.node_factory.clear_registered_nodes()
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
        for node in self.nodes_ordered(self.graph):
            node.process()

    @staticmethod
    def nodes_ordered(graph: NodeGraph) -> List[BaseNode]:
        visited = set()

        def visit(node):
            visited.add(node)
            parents = node.connected_input_nodes().values()
            children = node.connected_output_nodes().values()
            return [
                *(
                    j
                    for el in parents
                    for n in el
                    if n not in visited
                    for j in visit(n)
                  ),
                node,
                *(
                    j
                    for el in children
                    for n in el
                    if n not in visited
                    for j in visit(n)
                )
            ]

        sorted_nodes = [
            n
            for node in graph.all_nodes()
            if node not in visited
            for n in visit(node)
        ]
        return sorted_nodes

    def run(self):
        self.app.exec_()
