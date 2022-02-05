import sounddevice as sd

LATENCY = 'low'


def once(callback):
    result = callback()
    def ret():
        return result
    return ret


@once
def get_hostapi():
    order = [
        'JACK Audio Connection Kit',
        'ALSA'
    ]
    apis = sd.query_hostapis()
    for name in order:
        api = [x for x in apis if x['name'] == name]
        if api:
            return apis.index(api[0])


def device_map():
    return {it['name']: (it, j) for j, it in enumerate(sd.query_devices())
            if it["hostapi"] == get_hostapi()}


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
