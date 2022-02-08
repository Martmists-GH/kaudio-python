#pragma once

#ifdef __cplusplus
extern "C"
{
#endif	/* __cplusplus */

void* create_revmodel();
void delete_revmodel(void* model);
void mute(void* model);
void processmix(void* model, float *inputL, float *inputR, float *outputL, float *outputR, long numsamples, int skip);
void processreplace(void* model, float *inputL, float *inputR, float *outputL, float *outputR, long numsamples, int skip);
void setroomsize(void* model, float value);
float getroomsize(void* model);
void setdamp(void* model, float value);
float getdamp(void* model);
void setwet(void* model, float value);
float getwet(void* model);
void setdry(void* model, float value);
float getdry(void* model);
void setwidth(void* model, float value);
float getwidth(void* model);
void setmode(void* model, float value);
float getmode(void* model);

#ifdef __cplusplus
}
#endif	/* __cplusplus */
