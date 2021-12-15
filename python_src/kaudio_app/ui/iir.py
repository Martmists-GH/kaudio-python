from PySide2.QtCore import Signal
from PySide2.QtGui import QDoubleValidator
from PySide2.QtWidgets import QWidget, QHBoxLayout, QLabel, QLineEdit, QGroupBox, QVBoxLayout


class IIRRow(QWidget):
    set_value = Signal(float, int)

    def __init__(self, parent, name, index):
        super().__init__()

        self.setLayout(QHBoxLayout())
        self.layout().addWidget(QLabel(name))

        validator = QDoubleValidator(-10, 10, 10)
        validator.setNotation(QDoubleValidator.StandardNotation)

        line_edit = QLineEdit()
        line_edit.setValidator(validator)

        line_edit.setText(str((parent.node.coeffs_a + parent.node.coeffs_b)[index]))

        line_edit.editingFinished.connect(lambda *args, **kwargs: self.set_value.emit(float(line_edit.text()), index))
        self.layout().addWidget(line_edit)


class IIRWidget(QWidget):
    set_value = Signal(float, int)

    def __init__(self, node, name=None):
        super().__init__()
        self.node = node
        # self.setTitle(name)
        self.setLayout(QHBoxLayout())
        self.group_a = QWidget()
        self.group_b = QWidget()
        self.layout().addWidget(self.group_a)
        self.layout().addWidget(self.group_b)

    def set_node(self, node):
        self.node = node

    def set_order(self, order: int):
        group_a = QWidget()
        group_a.setLayout(QVBoxLayout())
        group_b = QWidget()
        group_b.setLayout(QVBoxLayout())

        for i in range(order+1):
            row_a = IIRRow(self, f"a{i}", i)
            row_b = IIRRow(self, f"b{i}", i+order+1)
            row_a.set_value.connect(lambda value, index: self.set_value.emit(value, index))
            row_b.set_value.connect(lambda value, index: self.set_value.emit(value, index))
            group_a.layout().addWidget(row_a)
            group_b.layout().addWidget(row_b)

        self.setFixedHeight(group_a.sizeHint().height())

        self.layout().replaceWidget(self.group_a, group_a)
        self.layout().replaceWidget(self.group_b, group_b)
        self.group_a = group_a
        self.group_b = group_b