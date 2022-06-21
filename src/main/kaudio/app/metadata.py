from typing import Type, List

from kaudio.app.nodes.base_nodes import BaseNode


def load_builtins(node_registry: List[Type[BaseNode]]):
    raise NotImplementedError()
