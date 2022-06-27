from typing import List

from NodeGraphQt import NodeGraph, NodesPaletteWidget
from PySide2.QtWidgets import QWidget, QVBoxLayout
from kaudio.app.nodes.base_nodes import BaseNode


class SidebarWidget(QWidget):
    def __init__(self, graph: NodeGraph):
        super().__init__()

        self.tracked_nodes = set()

        self.current_config_widget = None
        layout = QVBoxLayout()
        layout.addWidget(NodesPaletteWidget(node_graph=graph), 2)
        self.menu = menu = QWidget()
        menu.setLayout(QVBoxLayout())
        menu.setVisible(False)
        layout.addWidget(menu, 1)
        self.setLayout(layout)

        graph.node_created.connect(self.track_node)
        graph.nodes_deleted.connect(self.untrack_nodes)
        graph.node_selected.connect(self.track_node)
        graph.node_selection_changed.connect(self.selection_changed)

    def selection_changed(self, selected: List[BaseNode], deselected: List[BaseNode]):
        for node in deselected:
            if node in self.tracked_nodes:
                self.tracked_nodes.remove(node)

        for node in selected:
            self.tracked_nodes.add(node)
        self.update_widget()

    def untrack_nodes(self, nodes: List[BaseNode]):
        for node in nodes:
            matching = (n for n in self.tracked_nodes if n.id == node)
            for n in matching:
                self.tracked_nodes.remove(n)
        self.update_widget()

    def track_node(self, node: BaseNode):
        self.tracked_nodes.add(node)
        self.update_widget()

    def update_widget(self):
        num_selected = len(self.tracked_nodes)
        widget = self.current_config_widget
        if num_selected == 1 and widget is None:
            widget = list(self.tracked_nodes)[0].get_config_widget()
            if widget is not None:
                self.menu.layout().addWidget(widget)
                self.menu.setVisible(True)
                self.current_config_widget = widget
        elif num_selected != 1 and widget is not None:
            self.menu.layout().removeWidget(widget)
            self.menu.setVisible(False)
            self.current_config_widget = None
