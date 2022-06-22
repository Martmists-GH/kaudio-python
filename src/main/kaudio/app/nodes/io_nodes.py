from time import sleep

import numpy as np
from kaudio.app.nodes.base_nodes import StereoNode
from kaudio.app.utils.sounddevice_handler import device_map, open_stream
from kaudio.nodes.base import BaseNode as KBaseNode
from kaudio.nodes.util import (
    InputNode as InputNodeImpl,
    OutputNode as OutputNodeImpl
)


class InputNode(StereoNode):
    __identifier__ = "Devices"
    NODE_NAME = "Input Device"

    def __init__(self):
        super().__init__(True)
        self.stream = None
        self.config_combo("device",
                          "Input Device",
                          list(filter(lambda x: x[0]['max_input_channels'] >= 2, device_map().values())),
                          None,
                          lambda it: it[0]["name"])

    def __del__(self):
        if self.stream is not None:
            self.stream.stop()
            self.stream.close()
            self.stream = None

    def set_device(self, value):
        stream_idx = value[1]
        if self.stream is not None:
            stream = self.stream
            self.stream = None
            stream.stop()
            stream.close()

        stream = open_stream(
            stream_idx,
            True,
            True
        )
        stream.start()
        self.stream = stream

    def process(self):
        if self.stream is None:
            self.k_node.bufLeft = [0] * 1024
            self.k_node.bufRight = [0] * 1024
        else:
            while self.stream.read_available < 1024 * 2:
                sleep(0.0001)
            arr, overflowed = self.stream.read(1024)
            if overflowed:
                print("Overflowed")
                print(self.stream.read_available)
            self.k_node.bufLeft = list(arr[:1024, 0])
            self.k_node.bufRight = list(arr[:1024, 1])

    def get_node(self, stereo: bool) -> KBaseNode:
        return InputNodeImpl(True)


class OutputNode(StereoNode):
    __identifier__ = "Devices"
    NODE_NAME = "Output Device"

    def __init__(self):
        super().__init__(True)
        self.stream = None
        self.device = ({}, -1)
        self.config_combo("device",
                          "Input Device",
                          list(filter(lambda x: x[0]['max_output_channels'] >= 2, device_map().values())),
                          None,
                          lambda it: it[0]["name"])

    def __del__(self):
        if self.stream is not None:
            self.stream.stop()
            self.stream.close()
            self.stream = None

    def set_device(self, value):
        self.device = value
        stream_idx = value[1]
        if self.stream is not None:
            stream = self.stream
            self.stream = None
            stream.stop()
            stream.close()

        stream = open_stream(
            stream_idx,
            True,
            False
        )
        stream.start()
        self.stream = stream

    def process(self):
        if self.stream is None:
            return

        self.k_node.process()

        arr = np.zeros((1024, 2), dtype=np.float32)
        arr[:1024, 0] = self.k_node.bufLeft
        arr[:1024, 1] = self.k_node.bufRight

        while self.stream.write_available < 1024:
            sleep(0.0001)

        if self.stream.write(arr):
            print("Underflowed")
            print(self.stream.write_available)

    def get_node(self, stereo: bool) -> KBaseNode:
        return OutputNodeImpl(True)
