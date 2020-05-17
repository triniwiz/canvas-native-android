#include <jni.h>
#include <android/log.h>
#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>
#include "canvas_extras.h"

extern "C" JNIEXPORT void JNICALL
Java_com_github_triniwiz_canvas_WebGLRenderingContext_nativeGetVertexAttribOffset(JNIEnv *env,
                                                                                  jobject object,
                                                                                  jint index,
                                                                                  jint pname,
                                                                                  jobject buffer) {
    void *ptr = env->GetDirectBufferAddress(buffer);
    void **p = &ptr;
    glGetVertexAttribPointerv(static_cast<GLuint>(index), static_cast<GLenum>(pname), p);
}

extern "C" JNIEXPORT void JNICALL
Java_com_github_triniwiz_canvas_WebGLRenderingContext_nativeBindBuffer(JNIEnv *env, jobject object,
                                                                       jint target, jint buffer) {
    glBindBuffer(static_cast<GLenum>(target), static_cast<GLuint>(buffer));
}