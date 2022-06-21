#include "api/api.h"
#include "revmodel.hpp"

void*   create_revmodel() {
    auto* model = new revmodel;
    model->setmode(false);
    return model;
}

void    delete_revmodel(void* model) {
    delete (revmodel*)model;
}

void	mute(void* model) {
    ((revmodel*)model)->mute();
}

void	processmix(void* model, float *inputL, float *inputR, float *outputL, float *outputR, long numsamples, int skip) {
    ((revmodel*)model)->processmix(inputL, inputR, outputL, outputR, numsamples, skip);
}

void	processreplace(void* model, float *inputL, float *inputR, float *outputL, float *outputR, long numsamples, int skip) {
    ((revmodel*)model)->processreplace(inputL, inputR, outputL, outputR, numsamples, skip);
}

void	setroomsize(void* model, float value){
    ((revmodel*)model)->setroomsize(value);
}

float	getroomsize(void* model){
    return ((revmodel*)model)->getroomsize();
}

void	setdamp(void* model, float value){
    ((revmodel*)model)->setdamp(value);
}

float	getdamp(void* model){
    return ((revmodel*)model)->getdamp();
}

void	setwet(void* model, float value){
    ((revmodel*)model)->setwet(value);
}

float	getwet(void* model){
    return ((revmodel*)model)->getwet();
}

void	setdry(void* model, float value){
    ((revmodel*)model)->setdry(value);
}

float	getdry(void* model){
    return ((revmodel*)model)->getdry();
}

void	setwidth(void* model, float value){
    ((revmodel*)model)->setwidth(value);
}

float	getwidth(void* model){
    return ((revmodel*)model)->getwidth();
}

void	setmode(void* model, float value){
    ((revmodel*)model)->setmode(value);
}

float	getmode(void* model){
    return ((revmodel*)model)->getmode();
}
