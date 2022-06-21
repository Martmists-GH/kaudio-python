from NodeGraphQt import NodeGraph
from PySide2.QtWidgets import QWidget, QHBoxLayout

from kaudio.app.widgets.sidebar_widget import SidebarWidget


class MainWidget(QWidget):
    def __init__(self, graph: NodeGraph):
        super().__init__()
        layout = QHBoxLayout()
        layout.addWidget(SidebarWidget(), 4)
        layout.addWidget(graph.widget, 10)
        self.setLayout(layout)
