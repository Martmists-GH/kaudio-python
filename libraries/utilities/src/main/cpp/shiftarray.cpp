#include "shiftarray.h"
#include <cstring>
#include <cstdlib>
#include <cstdio>

inline int array_index(shift_array_t* array, int index) {
    int res = ((int)array->offset + index);
    if (res < 0) {
        res += array->size;
    }
    return res % array->size;
}

shift_array_t* array_alloc(unsigned int size) {
    shift_array_t* array = (shift_array_t*)malloc(sizeof(shift_array_t));
    array->size = size;
    array->offset = 0;
    array->data = (float*)malloc(size * sizeof(float));
    memset(array->data, 0, size * sizeof(float));
    return array;
}

void array_free(shift_array_t* array) {
    free(array->data);
    free(array);
}

float array_get(shift_array_t* array, int index) {
    return array->data[array_index(array, index)];
}

void array_set(shift_array_t* array, int index, float value) {
    array->data[array_index(array, index)] = value;
}

void array_shift(shift_array_t* array, int num) {
    array->offset = array_index(array, num);
}

void array_clear(shift_array_t* array) {
    memset(array->data, 0, array->size * sizeof(float));
}
