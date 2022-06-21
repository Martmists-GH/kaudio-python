from PySide2.QtWidgets import QWidget, QVBoxLayout


class SidebarWidget(QWidget):
    def __init__(self):
        super().__init__()
        layout = QVBoxLayout()
        self.setLayout(layout)
