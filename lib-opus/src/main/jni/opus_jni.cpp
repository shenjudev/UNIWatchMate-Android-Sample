//
// Created by 王文锋 on 2024/12/7.
//
#include <jni.h>
#include "common.h"
#include "opus.h"

#define FRAME_SIZE 320
#define MAX_FRAME_SIZE 6*320
#define MAX_PACKET_SIZE 1276

extern "C"
JNIEXPORT jlong JNICALL
Java_com_shenju_opus_OpusDecoderJni_createDecoder(JNIEnv *env, jobject thiz, jint sample_rate, jint channels) {
    int err;
    // 创建解码器
    OpusDecoder *decoder = opus_decoder_create(sample_rate, channels, &err);
    if (err < 0) {
        LOGE("Cannot create decoder: %s", opus_strerror(err));
        return 1;
    }

    return (jlong) decoder;
}
extern "C"
JNIEXPORT jint JNICALL
Java_com_shenju_opus_OpusDecoderJni_decode(JNIEnv *env, jobject thiz,  jlong decoder,
                                           jbyteArray opus_data, jbyteArray pcm_data, jint frame_size) {
    if (decoder == 0) {
        return -1;
    }

    int err;
    OpusDecoder *opd = (OpusDecoder *) decoder;

    // 获取pcm_data数组的本地内存指针
    int16_t *pcm = (int16_t *) env->GetByteArrayElements(pcm_data, JNI_FALSE);

    // 获取opus_data的长度
    int len = env->GetArrayLength(opus_data);

    // 解码操作
    int ret = opus_decode(opd, (const unsigned char *) env->GetByteArrayElements(opus_data, JNI_FALSE), len, pcm, MAX_FRAME_SIZE, 0);

    // 解码完成后释放内存并同步数据回Java/Kotlin层
    env->ReleaseByteArrayElements(opus_data, (jbyte *) env->GetByteArrayElements(opus_data, JNI_FALSE), JNI_ABORT);

    // 使用JNI_COMMIT将pcm_data的数据更新回Kotlin层
    env->ReleaseByteArrayElements(pcm_data, (jbyte *) pcm, JNI_COMMIT);  // 修改为JNI_COMMIT

    return ret;
}
extern "C"
JNIEXPORT void JNICALL
Java_com_shenju_opus_OpusDecoderJni_destroyDecoder(JNIEnv *env, jobject thiz, jlong decoder) {
    opus_decoder_destroy((OpusDecoder *) decoder);
}