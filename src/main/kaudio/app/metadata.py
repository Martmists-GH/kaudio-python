from typing import Type, List

from kaudio.app.nodes.base_nodes import BaseNode
from kaudio.app.nodes.effect_nodes import Bs2bNode, EqualizerNode, EqualLoudnessNode, FIRNode, IIRNode, ReverbNode, \
    TubeSimulatorNode, VolumeNode

from kaudio.app.nodes.io_nodes import InputNode, OutputNode

from kaudio.app.nodes.util_nodes import SplitterNode, CombinerNode


def load_builtins(node_registry: List[Type[BaseNode]]):
    node_registry.extend([
        Bs2bNode,
        EqualizerNode,
        EqualLoudnessNode,
        FIRNode,
        IIRNode,
        ReverbNode,
        TubeSimulatorNode,
        VolumeNode,

        InputNode,
        OutputNode,

        CombinerNode,
        SplitterNode,
    ])
