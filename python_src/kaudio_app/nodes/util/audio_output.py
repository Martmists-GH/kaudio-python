from time import sleep

import kaudio
import numpy as np
from PySide2.QtWidgets import QWidget

from kaudio_app.nodes.abstract.base_node import BaseNode
from kaudio_app.sounddevice_handler import device_map, open_stream


class AudioOutput(BaseNode):
    __identifier__ = BaseNode.__identifier__ + ".standard"
    NODE_NAME = "Audio Output"

    def __init__(self):
        self.device = ""

        super().__init__()
        self.stream = None
        self.set_property("device", [k for k, v in device_map().items() if v[0]['max_output_channels'] >= 2][0])

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
                False
            )
            stream.start()
            self.stream = stream

        return kaudio.OutputNode(stereo)

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
                False
            )
            stream.start()
            self.stream = stream
        else:
            super().set_property(name, value)

    def process(self):
        self.node.process()

        if self.stream is None:
            return

        if self.stereo:
            arr = np.zeros((1024, 2), dtype=np.float32)
            arr[:1024, 0] = self.node.buffer_left
            arr[:1024, 1] = self.node.buffer_right
        else:
            arr = np.zeros((1024, 1), dtype=np.float32)
            arr[:1024, 0] = self.node.buffer

        while self.stream.write_available < 1024:
            sleep(0.0001)

        if self.stream.write(arr):
            print("Underflowed")
            print(self.stream.write_available)

    def configure_config_widget(self, widget: QWidget):
        super().configure_config_widget(widget)
        self.config_combobox("device", "Output device", self.device, [k for k, v in device_map().items() if v[0]['max_output_channels'] >= 2], widget)
