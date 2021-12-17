from time import sleep

import kaudio
from PySide2.QtWidgets import QWidget

from kaudio_app.nodes.abstract.base_node import BaseNode
from kaudio_app.sounddevice_handler import device_map, open_stream


class AudioInput(BaseNode):
    __identifier__ = BaseNode.__identifier__ + ".standard"
    NODE_NAME = "Audio Input"

    def __init__(self):
        self.device = ""

        super().__init__()
        self.stream = None
        self.set_property("device", "default")

    def __del__(self):
        if self.stream is not None:
            self.stream.stop()
            self.stream.close()
            self.stream = None

    def get_new_node(self, stereo: bool) -> kaudio.BaseNode:
        if stereo != self.stereo and self.stream is not None:
            stream = self.stream
            self.stream = None
            stream.stop()
            stream.close()
            stream = open_stream(
                device_map()[self.device][1],
                stereo,
                True
            )
            stream.start()
            self.stream = stream

        return kaudio.InputNode(stereo)

    def set_property(self, name, value):
        if name == "device":
            self.device = value
            stream_idx = device_map()[value][1]

            if self.stream is not None:
                stream = self.stream
                self.stream = None
                stream.stop()
                stream.close()

            stream = open_stream(
                stream_idx,
                self.stereo,
                True
            )
            stream.start()
            self.stream = stream
        else:
            super().set_property(name, value)

    def process(self):
        if self.stream is None:
            if self.stereo:
                self.node.buffer_left = [0] * 1024
                self.node.buffer_right = [0] * 1024
            else:
                self.node.buffer = [0] * 1024
        else:
            while self.stream.read_available < 1024 * (self.stereo + 1):
                sleep(0.0001)
            arr, overflowed = self.stream.read(1024)
            if overflowed:
                print(self.stream.read_available)
                print("Overflowed")
            if self.stereo:
                self.node.buffer_left = list(arr[:1024, 0])
                self.node.buffer_right = list(arr[:1024, 1])
            else:
                self.node.buffer = list(arr[:1024, 0])

        self.node.process()

    def configure_config_widget(self, widget: QWidget):
        super().configure_config_widget(widget)
        self.config_combobox("device", "Input device", self.device, [k for k, v in device_map().items() if v[0]['max_input_channels'] >= 2], widget)
