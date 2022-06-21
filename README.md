# KAudio Python Wrapper

A reimplementation of my kaudio library for Python 3.10+

# Application

An application is bundled by default:

```
$ pip install Qt.py          # required due to an issue in NodeGraphQt
$ pip install --user .[app]  # on zsh: pip install --user .\[app\]
$ python3 -m kaudio.app
```

# Usage

### Installation

```
$ pip install .
```

### Code

```python
import kaudio

input_node = kaudio.InputNode(stereo=False)
output_node = kaudio.OutputNode(stereo=False)
effect_node = kaudio.VolumeNode(stereo=False)

input_node.connect("output", effect_node, "input")
effect_node.connect("output", output_node, "input")

effect_node.gain = 3.0

input_node.buffer = [<1024 floats>]
input_node.process()  # Send to effect_node
effect_node.process()  # Apply gain and send to output_node
output_node.process()  # Write to out buffer

result_buffer = output_node.buffer
```
