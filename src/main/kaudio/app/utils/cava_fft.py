from math import log10, log2
import numpy as np

from kaudio.app.utils.cupy_support import to_backend, backend, from_backend


class CavaFFT:
    """
    A modified implementation of cava's "FFT"
    Link: https://github.com/karlstav/cava/blob/master/cavacore.c
    """
    def __init__(self, num_bars: int, channels: int, autosens: bool, noise_reduction: float, low_cutoff: int, high_cutoff: int):
        treble_buffer_size = 1024
        assert num_bars <= treble_buffer_size / 2 + 1

        self.channels = channels
        self.num_bars = num_bars
        self.rate = 48000
        self.sens = True
        self.autosens = autosens
        self.average_max = 0
        self.sens_init = True
        self.framerate = 75
        self.frame_skip = 1
        self.noise_reduction = noise_reduction
        self.height = 10  # never actually set in cava?
        self.average_max = log10(self.height) * 0.05
        self.fft_bass_buffer_size = treble_buffer_size * 8
        self.fft_mid_buffer_size = treble_buffer_size * 4
        self.fft_treble_buffer_size = treble_buffer_size

        self.input_buffer_size = self.fft_bass_buffer_size

        self.input_buffer = np.zeros((self.input_buffer_size, channels))
        self.fft_lower_cutoff = np.zeros((num_bars + 1,))
        self.fft_upper_cutoff = np.zeros((num_bars + 1,))
        self.eq = np.zeros((num_bars + 1,))
        self.cutoff_frequency = np.zeros((num_bars + 1,))

        self.cava_fall = np.zeros((num_bars, channels))
        self.cava_mem = np.zeros((num_bars, channels))
        self.cava_peak = np.zeros((num_bars, channels))
        self.prev_cava_out = np.zeros((num_bars, channels))

        self.bass_window = np.dstack([np.hanning(self.fft_bass_buffer_size)]*self.channels).reshape(self.fft_bass_buffer_size, self.channels)
        self.mid_window = np.dstack([np.hanning(self.fft_mid_buffer_size)]*self.channels).reshape(self.fft_mid_buffer_size, self.channels)
        self.treble_window = np.dstack([np.hanning(self.fft_treble_buffer_size)]*self.channels).reshape(self.fft_treble_buffer_size, self.channels)

        self.in_bass = np.zeros((self.fft_bass_buffer_size, channels))
        self.in_mid = np.zeros((self.fft_mid_buffer_size, channels))
        self.in_treble = np.zeros((self.fft_treble_buffer_size, channels))

        self.out_bass = np.zeros((self.fft_bass_buffer_size, channels))
        self.out_mid = np.zeros((self.fft_mid_buffer_size, channels))
        self.out_treble = np.zeros((self.fft_treble_buffer_size, channels))

        lower_cutoff = low_cutoff
        upper_cutoff = high_cutoff
        bass_cutoff = 100
        treble_cutoff = 500

        frequency_constant = log10(lower_cutoff / upper_cutoff) / (1 / (num_bars + 1) - 1)
        relative_cutoff = np.zeros((self.fft_treble_buffer_size,))

        self.bass_cutoff_bar = -1
        self.treble_cutoff_bar = -1
        first_bar = True
        first_treble_bar = 0
        bar_buffer = np.zeros((num_bars+1,))

        for n in range(self.num_bars + 1):
            bar_distribution_coefficient = -frequency_constant
            bar_distribution_coefficient += (n + 1) / (num_bars + 1) * frequency_constant
            self.cutoff_frequency[n] = upper_cutoff * 10 ** bar_distribution_coefficient

            if n > 0:
                if self.cutoff_frequency[n-1] >= self.cutoff_frequency[n] and self.cutoff_frequency[n-1] > bass_cutoff:
                    self.cutoff_frequency[n] = self.cutoff_frequency[n-1] + (self.cutoff_frequency[n-1] - self.cutoff_frequency[n-2])

            relative_cutoff[n] = self.cutoff_frequency[n] / (self.rate / 2)
            self.eq[n] = self.cutoff_frequency[n] ** 1
            self.eq[n] /= 2 ** 18
            self.eq[n] /= log2(self.fft_bass_buffer_size)

            if self.cutoff_frequency[n] > bass_cutoff:
                bar_buffer[n] = 1
                self.fft_lower_cutoff[n] = int(relative_cutoff[n] * (self.fft_bass_buffer_size / 2))
                self.bass_cutoff_bar += 1
                self.treble_cutoff_bar += 1
                if self.bass_cutoff_bar > 0:
                    first_bar = False

                if self.fft_lower_cutoff[n] > self.fft_bass_buffer_size / 2:
                    self.fft_lower_cutoff[n] = self.fft_bass_buffer_size / 2

            elif self.cutoff_frequency[n] < treble_cutoff:
                bar_buffer[n] = 2
                self.fft_lower_cutoff[n] = int(relative_cutoff[n] * (self.fft_mid_buffer_size / 2))
                self.treble_cutoff_bar += 1
                if self.treble_cutoff_bar - self.bass_cutoff_bar == 1:
                    first_bar = True
                    if n > 0:
                        self.fft_upper_cutoff[n-1] = int(relative_cutoff[n] * (self.fft_bass_buffer_size / 2))
                else:
                    first_bar = False

                if self.fft_lower_cutoff[n] > self.fft_treble_buffer_size / 2:
                    self.fft_lower_cutoff[n] = self.fft_treble_buffer_size / 2

            else:
                bar_buffer[n] = 3
                self.fft_lower_cutoff[n] = int(relative_cutoff[n] * (self.fft_treble_buffer_size / 2))
                first_treble_bar += 1
                if first_treble_bar == 1:
                    first_bar = True
                    if n > 0:
                        self.fft_upper_cutoff[n-1] = int(relative_cutoff[n] * (self.fft_bass_buffer_size / 2))
                else:
                    first_bar = False

                if self.fft_lower_cutoff[n] > self.fft_treble_buffer_size / 2:
                    self.fft_lower_cutoff[n] = self.fft_treble_buffer_size / 2

            if n > 0:
                if not first_bar:
                    self.fft_upper_cutoff[n-1] = self.fft_lower_cutoff[n] - 1
                    if self.fft_lower_cutoff[n] <= self.fft_lower_cutoff[n-1]:
                        room_for_more = False

                        if bar_buffer[n] == 1:
                            if self.fft_lower_cutoff[n-1] + 1 < self.fft_bass_buffer_size / 2 + 1:
                                room_for_more = True
                        elif bar_buffer[n] == 2:
                            if self.fft_lower_cutoff[n-1] + 1 < self.fft_mid_buffer_size / 2 + 1:
                                room_for_more = True
                        elif bar_buffer[n] == 3:
                            if self.fft_lower_cutoff[n-1] + 1 < self.fft_treble_buffer_size / 2 + 1:
                                room_for_more = True

                        if room_for_more:
                            self.fft_lower_cutoff[n] = self.fft_lower_cutoff[n-1] + 1
                            self.fft_upper_cutoff[n - 1] = self.fft_lower_cutoff[n] - 1

                            if bar_buffer[n] == 1:
                                relative_cutoff[n] = self.fft_lower_cutoff[n] / (self.fft_bass_buffer_size / 2)
                            elif bar_buffer[n] == 2:
                                relative_cutoff[n] = self.fft_lower_cutoff[n] / (self.fft_mid_buffer_size / 2)
                            elif bar_buffer[n] == 3:
                                relative_cutoff[n] = self.fft_lower_cutoff[n] / (self.fft_treble_buffer_size / 2)

                            self.cutoff_frequency[n] = relative_cutoff[n] * (self.rate / 2)
                elif self.fft_upper_cutoff[n-1] <= self.fft_lower_cutoff[n-1]:
                    self.fft_upper_cutoff[n-1] = self.fft_lower_cutoff[n-1] + 1

    def execute(self, input_data: np.ndarray) -> np.ndarray:
        silence = not input_data.any()
        num_samples = min(input_data.shape[0], self.input_buffer_size)

        if num_samples > 0:
            self.framerate -= self.framerate / 64
            self.framerate += ((self.rate * self.frame_skip) / num_samples) / 64
            self.frame_skip = 1

        self.input_buffer = np.roll(self.input_buffer, -num_samples, axis=0)
        self.input_buffer[-num_samples::, ::] = input_data

        idx = 0 if self.channels == 1 else slice()

        self.in_bass[::, idx] = self.input_buffer[-self.fft_bass_buffer_size:, idx]
        self.in_mid[::, idx] = self.input_buffer[-self.fft_mid_buffer_size:, idx]
        self.in_treble[::, idx] = self.input_buffer[-self.fft_treble_buffer_size:, idx]

        self.in_bass *= self.bass_window
        self.in_mid *= self.mid_window
        self.in_treble *= self.treble_window

        self.out_bass = self.single_fft(self.in_bass)
        self.out_mid = self.single_fft(self.in_mid)
        self.out_treble = self.single_fft(self.in_treble)

        cava_out = np.zeros((self.num_bars, self.channels))

        for n in range(self.num_bars):
            for i in range(int(self.fft_lower_cutoff[n]), int(self.fft_upper_cutoff[n]+1)):
                if n <= self.bass_cutoff_bar:
                    cava_out[n, ::] += self.out_bass[i, ::]
                elif n <= self.treble_cutoff_bar:
                    cava_out[n, ::] += self.out_mid[i, ::]
                else:
                    cava_out[n, ::] += self.out_treble[i, ::]
            cava_out[n, ::] /= self.fft_upper_cutoff[n] - self.fft_lower_cutoff[n] + 1
            cava_out[n, ::] *= self.eq[n]

        for n in range(self.num_bars):
            if self.autosens:
                cava_out[n, ::] *= self.sens
            else:
                for i in range(self.channels):
                    if cava_out[n, i] > self.average_max:
                        self.average_max -= self.average_max / 64
                        self.average_max += cava_out[n, i] / 64

        overshoot = False
        gravity_mod = ((60 / self.framerate) ** 2.5) * 1.54 / self.noise_reduction
        if gravity_mod < 1:
            gravity_mod = 1

        for n in range(self.num_bars):
            for i in range(self.channels):
                if cava_out[n, i] < self.prev_cava_out[n, i] and self.noise_reduction > 0.1:
                    cava_out[n, i] = self.cava_peak[n, i] * (1000 - (self.cava_fall[n, i]**2 * gravity_mod)) / 1000
                    if cava_out[n, i] < 0:
                        cava_out[n, i] = 0
                    self.cava_fall[n, i] += 1
                else:
                    self.cava_peak[n, i] = cava_out[n, i]
                    self.cava_fall[n, i] = 0
                self.prev_cava_out[n, i] = cava_out[n, i]

                cava_out[n, i] = self.cava_mem[n, i] * self.noise_reduction + cava_out[n, i]
                self.cava_mem[n, i] = cava_out[n, i]
                if self.autosens:
                    diff = 1000 - cava_out[n, i]
                    if diff < 0:
                        diff = 0
                    div = 1 / (diff + 1)
                    self.cava_mem[n, i] *= (1 - div / 20)

                    if cava_out[n, i] > 1000:
                        overshoot = True
                    cava_out[n, i] /= 1000

        if self.autosens:
            if overshoot:
                self.sens *= 0.98
                self.sens_init = False
            elif not silence:
                self.sens *= 1.001
                if self.sens_init:
                    self.sens *= 1.1

        return cava_out

    @staticmethod
    def smooth_monstercat(bars: np.ndarray, waves: bool, monstercat: int) -> np.ndarray:
        if waves:
            for z in range(bars.shape[0]):
                bars[z] /= 1.25
                for m_y in range(z-1, -1, -1):
                    de = z - m_y
                    bars[m_y] = max(bars[z] - de**2, bars[m_y])
                for m_y in range(z+1, bars.shape[0]):
                    de = m_y - z
                    bars[m_y] = max(bars[z] - de**2, bars[m_y])
        elif monstercat > 0:
            for z in range(bars.shape[0]):
                for m_y in range(z-1, -1, -1):
                    de = z - m_y
                    bars[m_y] = max(bars[z] - monstercat**de, bars[m_y])
                for m_y in range(z+1, bars.shape[0]):
                    de = m_y - z
                    bars[m_y] = max(bars[z] - monstercat**de, bars[m_y])
        return bars

    @staticmethod
    def single_fft(arr: np.ndarray) -> np.ndarray:
        backend_array = to_backend(arr)
        amplitudes = backend.abs(backend.fft.fft(backend_array))
        return from_backend(amplitudes)
