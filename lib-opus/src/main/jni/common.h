//
// Created by wangwenfeng on 2023/7/5.
//

#ifndef CAMERACAPTURER_COMMON_H
#define CAMERACAPTURER_COMMON_H

#include <fstream>
#include <string>
#include <vector>

#ifdef ANDROID

#include <android/log.h>

#define TAG "JNI"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)
#define LOGF(...) __android_log_print(ANDROID_LOG_FATAL, TAG, __VA_ARGS__)

#else
#define LOGD(...) printf(__VA_ARGS__)
#define LOGI(...) printf(__VA_ARGS__)
#define LOGW(...) printf(__VA_ARGS__)
#define LOGE(...) printf(__VA_ARGS__)
#define LOGF(...) printf(__VA_ARGS__)
#endif

#include <iostream>
#include <streambuf>

// 自定义 streambuf，用于重定向 std::cout
class AndroidLogBuf : public std::streambuf {
protected:
    virtual int_type overflow(int_type v) {
        if (v == '\n') {
//            __android_log_print(ANDROID_LOG_INFO, "JNI", "%s", buffer_.c_str());
            buffer_.clear();
        } else {
            buffer_ += static_cast<char>(v);
        }
        return v;
    }

private:
    std::string buffer_;
};

#endif //CAMERACAPTURER_COMMON_H
