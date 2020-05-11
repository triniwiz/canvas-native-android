//
// Created by Osei Fortune on 5/10/20.
//

#ifndef CANVAS_ANDROID_CANVAS_EXTRAS_H
#define CANVAS_ANDROID_CANVAS_EXTRAS_H

#include <android/log.h>

#if DYNAMIC_ES3
#include "gl3stub.h"

#else
#if __ANDROID_API__ >= 24
#include <GLES3/gl32.h>
#elif __ANDROID_API__ >= 21
#include <GLES3/gl31.h>
#else
#include <GLES3/gl3.h>
#endif

#endif

#endif //CANVAS_ANDROID_CANVAS_EXTRAS_H
