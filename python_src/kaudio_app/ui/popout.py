from PySide2.QtCore import Signal
from PySide2.QtWidgets import QWidget


class PopoutWidget(QWidget):
    on_close = Signal()

    def __init__(self):
        super().__init__()

    def closeEvent(self, event):
        self.on_close.emit()
        super().closeEvent(event)
