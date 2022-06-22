from typing import Union

from sounddevice import query_hostapis, query_devices, InputStream, OutputStream

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
    apis = query_hostapis()
    for name in order:
        for api in filter(lambda x: x['name'] == name, apis):
            return apis.index(api)


def device_map():
    return {it['name']: (it, j) for j, it in enumerate(query_devices())
            if it["hostapi"] == get_hostapi()}


def open_stream(index: int, stereo: bool, is_input: bool) -> Union[InputStream, OutputStream]:
    if is_input:
        return InputStream(device=index,
                           channels=1 + stereo,
                           latency=LATENCY,
                           samplerate=48000,
                           blocksize=1024,
                           dtype='float32')
    else:
        return OutputStream(device=index,
                            channels=1 + stereo,
                            latency=LATENCY,
                            samplerate=48000,
                            blocksize=1024,
                            dtype='float32')
