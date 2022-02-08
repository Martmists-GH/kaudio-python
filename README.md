# KAudio Python Wrapper

A reimplementation of my kaudio library for Python.

# Application

An application is bundled by default:

```
$ pip install .
$ cd python_src
$ python3 -m kaudio_app
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
