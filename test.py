input("Press enter to start")

import kaudio
import numpy as np
import matplotlib.pyplot as plt


def get_bounds(fft_results: np.ndarray, samplerate: int):
    lowest = min([-20, np.min(fft_results[1: samplerate // 2 - 1])])
    highest = max([20, np.max(fft_results[1: samplerate // 2 - 1])])
    return lowest, highest


def show_response(filter):
    samplerate = 48000
    size = 1024
    in_node = kaudio.InputNode()
    out_node = kaudio.OutputNode()
    in_node.connect_stereo(filter)
    filter.connect_stereo(out_node)
    in_node.buffer_left = [1] + ([0] * 1023)
    in_node.process()
    filter.process()
    out_node.process()
    outputs = out_node.buffer_left
    filler = [0] * (samplerate - size)  # zero-padding
    outputs += filler
    fft_out = np.abs(np.fft.fft(outputs))
    fft_db = 20 * np.log10(fft_out)

    # Frequencies on log scale from 24 to nyquist frequency
    plt.xlim(24, samplerate / 2 - 1)
    plt.xlabel("Frequency (Hz)")
    plt.xscale("log")

    # Display within reasonable bounds
    bounds = get_bounds(fft_db, samplerate)
    plt.ylim(max([-80, bounds[0]]), min([80, bounds[1]]))
    plt.ylabel("Gain (dB)")

    plt.plot(fft_db)
    plt.show()


def main():
    show_response(
        kaudio.EqualLoudnessNode(True)
    )


if __name__ == "__main__":
    main()
