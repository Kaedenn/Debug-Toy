//
// Created by kaedenn on 2/16/20.
//

#ifndef DEBUG_TOY_PARTICLE_NATIVE_H
#define DEBUG_TOY_PARTICLE_NATIVE_H

#include <jni.h>

extern "C"
JNIEXPORT void JNICALL Java_net_kaedenn_debugtoy_SurfaceAnimation_animateNative
        (JNIEnv *, jobject, jobjectArray, jfloatArray, jfloatArray);

#endif //DEBUG_TOY_PARTICLE_NATIVE_H
