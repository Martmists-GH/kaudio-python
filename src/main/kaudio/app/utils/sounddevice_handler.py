import sounddevice as sd

LATENCY = 'low'


def once(callback):
    callback.__result__ = None
    callback.__isset__ = False

    def ret():
        if not callback.__isset__:
            callback.__result__ = callback()
            callback.__isset__ = True

        return callback.__result__

    return ret


@once
def get_hostapi():
    order = [
        'JACK Audio Connection Kit',
        'ALSA'
    ]
    apis = sd.query_hostapis()
    for name in order:
        for api in filter(lambda x: x['name'] == name, apis):
            return apis.index(api)


def device_map():
    return {it['name']: (it, j) for j, it in enumerate(sd.query_devices())
            if it["hostapi"] == get_hostapi()}


def open_stream(index: int, stereo: bool, is_input: bool):
    if is_input:
        return sd.InputStream(device=index,
                              channels=1 + stereo,
                              latency=LATENCY,
                              samplerate=48000,
                              blocksize=1024,
                              dtype='float32')
    else:
        return sd.OutputStream(device=index,
                               channels=1 + stereo,
                               latency=LATENCY,
                               samplerate=48000,
                               blocksize=1024,
                               dtype='float32')
