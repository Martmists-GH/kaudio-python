#pragma once

#ifdef __cplusplus
extern "C" {
#endif

typedef struct {
    unsigned int size;
    int offset;
    float* data;
} shift_array_t;

shift_array_t* array_alloc(unsigned int size);
void array_free(shift_array_t* array);
float array_get(shift_array_t* array, int index);
void array_set(shift_array_t* array, int index, float value);
void array_shift(shift_array_t* array, int num);
void array_clear(shift_array_t* array);

#ifdef __cplusplus
}
#endif
