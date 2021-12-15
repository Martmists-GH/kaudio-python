from typing import Tuple

from PySide2.QtCore import Qt, Signal
from PySide2.QtWidgets import QGroupBox, QHBoxLayout, QLabel, QSlider, QComboBox, QWidget


class IntSlider(QWidget):
    on_set = Signal(float)

    def __init__(self, label: str, value: int, range: Tuple[int, int], step_size: int = 1):
        super().__init__()
        self.setLayout(QHBoxLayout())

        self.label = QLabel(label)
        self.value = QLabel("")
        slider = QSlider(Qt.Horizontal)
        slider.setMinimum(range[0])
        slider.setMaximum(range[1])
        slider.setValue(value)
        slider.setSingleStep(step_size)
        slider.valueChanged.connect(self.on_value_changed)

        self.slider = slider

        self.layout().addWidget(self.label)
        self.layout().addWidget(self.slider)
        self.layout().addWidget(self.value)
        self.on_value_changed(value)

    def on_value_changed(self, value):
        self.value.setText(str(value))
        self.on_set.emit(value)


class FloatSlider(IntSlider):
    def __init__(self, label: str, value: float, range: Tuple[float, float], step_size: float = 0.1):
        self.scale = 1 / step_size
        super().__init__(label,
                         int(value * self.scale),
                         (int(range[0] * self.scale), int(range[1] * self.scale)),
                         int(step_size * self.scale))

    def on_value_changed(self, value):
        value = value / self.scale
        self.value.setText(str(value))
        self.on_set.emit(value)


class ComboBox(QWidget):
    on_set = Signal(str)

    def __init__(self, label: str, options: list):
        super().__init__()
        self.setLayout(QHBoxLayout())

        _label = QLabel(label)
        combo = QComboBox()
        combo.addItems(options)

        combo.currentTextChanged.connect(lambda x: self.on_set.emit(x))

        self.layout().addWidget(_label)
        self.layout().addWidget(combo)