import sounddevice as sd

LATENCY = 'low'


def device_map():
    return {it['name']: (it, j) for j, it in enumerate(sd.query_devices())}


def open_stream(index: int, stereo: bool, is_input: bool):
    if is_input:
        return sd.InputStream(device=index,
                              channels=2 if stereo else 1,
                              latency=LATENCY,
                              samplerate=48000,
                              blocksize=1024,
                              dtype='float32')
    else:
        return sd.OutputStream(device=index,
                               channels=2 if stereo else 1,
                               latency=LATENCY,
                               samplerate=48000,
                               blocksize=1024,
                               dtype='float32')
