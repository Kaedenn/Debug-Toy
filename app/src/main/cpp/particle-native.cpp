//
// Created by kaedenn on 2/16/20.
//

#include "particle-native.h"
#include <iostream>

extern "C"
{

JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env;
    if (vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }

    // Find your class. JNI_OnLoad is called from the correct class loader context for this to work.
    jclass c = env->FindClass("net/kaedenn/debugtoy/SurfaceAnimation");
    if (c == nullptr) return JNI_ERR;

    /*
    // Register your class' native methods.
    static const JNINativeMethod methods[] = {
            {"nativeFoo", "()V",                    reinterpret_cast<void *>(nativeFoo)},
            {"nativeBar", "(Ljava/lang/String;I)Z", reinterpret_cast<void *>(nativeBar)},
    };
    int rc = env->RegisterNatives(c, methods, sizeof(methods) / sizeof(JNINativeMethod));
    if (rc != JNI_OK) return rc;
     */

    return JNI_VERSION_1_6;
}

JNIEXPORT void JNICALL Java_net_kaedenn_debugtoy_SurfaceAnimation_animateNative
        (JNIEnv *env, jobject thisp, jobjectArray particles, jfloatArray wh, jfloatArray ddxy) {
    /*
    jclass cls = env->FindClass("android/util/Log");
    if (cls == nullptr)
        return;
    jmethodID mid = env->GetStaticMethodID(cls, "d", "(Ljava/lang/String;Ljava/lang/String;)I");
    if (mid == nullptr)
        return;
    jstring logTag = env->NewStringUTF("native");
    jstring logMessage = env->NewStringUTF("Native function called");
    int result = env->CallStaticIntMethod(cls, mid, logTag, logMessage);
    env->ReleaseStringUTFChars(logTag, "native");
    env->ReleaseStringUTFChars(logMessage, "Native message called");
    */
}

JNIEXPORT void JNICALL Java_net_kaedenn_debugtoy_SurfaceAnimation_funcNative
        (JNIEnv *env, jobject thisp) {
    std::cerr << "Called funcNative()" << std::endl;
}
} /* extern "C" */

