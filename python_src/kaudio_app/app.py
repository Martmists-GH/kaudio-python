from math import floor
from threading import Lock

from NodeGraphQt import NodeGraph, setup_context_menu, NodesPaletteWidget
from PySide2.QtCore import QTimer
from PySide2.QtWidgets import QWidget, QVBoxLayout, QTabWidget, QApplication, QHBoxLayout, QMainWindow

from kaudio_app.nodes.effect.advanced.biquad_node import ButterworthNode
from kaudio_app.nodes.effect.advanced.iir_node import IIRNode
from kaudio_app.nodes.effect.simple.bs2b_node import Bs2bNode
from kaudio_app.nodes.effect.simple.equal_loudness_node import EqualLoudnessNode
from kaudio_app.nodes.effect.simple.volume_node import VolumeNode
from kaudio_app.nodes.util.audio_input import AudioInput
from kaudio_app.nodes.util.audio_output import AudioOutput
from kaudio_app.nodes.util.combiner import Combiner
from kaudio_app.nodes.util.splitter import Splitter
from kaudio_app.nodes.util.visualizer import Visualizer


class App:
    INSTANCE = None

    def __init__(self):
        App.INSTANCE = self

        node_types = [
            AudioInput,
            AudioOutput,
            Splitter,
            Combiner,

            Visualizer,

            EqualLoudnessNode,
            VolumeNode,
            Bs2bNode,

            IIRNode,
            ButterworthNode,
        ]

        self.app = QApplication([])
        self.window = QMainWindow()

        self.graph = NodeGraph()
        self.lock = Lock()

        setup_context_menu(self.graph)
        self.menu = self.graph.get_context_menu('graph')
        self.node_menu = self.graph.get_context_menu('nodes')

        for _type in node_types:
            self.node_menu.add_command("Popout Menu", func=self.popout_widget, node_class=_type)

        self.graph.register_nodes(node_types)
        self.graph.nodes_deleted.connect(lambda deleted: self.toggle_node_props(removed=deleted))
        self.graph.node_selection_changed.connect(self.toggle_node_props)

        self.timer = QTimer()
        self.timer.setInterval(floor(1024 / 48000 * 1000) - 10)
        self.timer.timeout.connect(self.run_graph)
        self.timer.start()

        self.nodes_palette = NodesPaletteWidget(node_graph=self.graph)

        self.props_widget = QWidget()
        self.props_widget.setLayout(QVBoxLayout())
        self.props_child = QTabWidget()
        self.props_widget.layout().addWidget(self.props_child)
        self.props_widget.setVisible(False)

        layout = QHBoxLayout()
        sub_layout = QVBoxLayout()
        sub_layout.addWidget(self.nodes_palette, 2)
        sub_layout.addWidget(self.props_widget, 1)
        sub_widget = QWidget()
        sub_widget.setLayout(sub_layout)
        layout.addWidget(sub_widget, 4)
        layout.addWidget(self.graph.widget, 10)

        self.main_widget = QWidget()
        self.main_widget.setLayout(layout)

        self.window.setCentralWidget(self.main_widget)
        self.window.showMaximized()

        self.selected_nodes = set()

    def popout_widget(self, graph, node):
        node.popout()

    def toggle_node_props(self, added=None, removed=None):
        added = added or []
        removed = removed or []

        self.props_widget.setVisible(False)

        # Update selected nodes
        for r in removed:
            if r in self.selected_nodes:  # They are also selected when first added to the graph
                self.selected_nodes.remove(r)
        for a in added:
            self.selected_nodes.add(a)

        if len(self.selected_nodes) == 1:
            # Create new widget
            node = list(self.selected_nodes)[0]
            if not node.popped_out:
                layout = self.props_child.parent().layout()
                widget = self.props_child
                new = QTabWidget()
                node.set_config_widget(new)
                layout.replaceWidget(widget, new)
                widget.deleteLater()
                self.props_child = new
                self.props_widget.setVisible(True)

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
