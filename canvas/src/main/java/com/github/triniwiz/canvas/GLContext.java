package com.github.triniwiz.canvas;

import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;

import static android.os.Process.THREAD_PRIORITY_DEFAULT;
import static android.os.Process.THREAD_PRIORITY_FOREGROUND;

/**
 * Created by triniwiz on 6/9/20
 */
public class GLContext {
    private BlockingQueue<Runnable> mQueue = new LinkedBlockingQueue<>();
    private GLThread mGLThread;

    private EGLDisplay mEGLDisplay;
    private EGLSurface mEGLSurface;
    private EGLContext mEGLContext;
    private EGLConfig mEGLConfig;
    private EGL10 mEGL;

    WeakReference<CanvasView> reference;

    public boolean isHeadless() {
        if (mGLThread != null) {
            return mGLThread.mSurface== null;
        }
        return true;
    }

    public void queueEvent(Runnable runnable) {
        mQueue.add(runnable);
    }

    public void init(Object texture) {
        if (mGLThread != null) {
            return;
        }
        mGLThread = new GLThread(texture);
        mGLThread.setPriority(Thread.MIN_PRIORITY);
        mGLThread.start();
    }

    public void flush() {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                if (reference != null) {
                    CanvasView canvasView = reference.get();
                    if (canvasView != null && canvasView.canvas != 0 && canvasView.pendingInvalidate) {
                        CanvasView.nativeFlush(canvasView.canvas);
                        if (!swapBuffers(mEGLSurface)) {
                            Log.e("GLContext", "Cannot swap buffers!");
                        }
                        canvasView.pendingInvalidate = false;
                    } else {
                        // WebGL
                        if (!swapBuffers(mEGLSurface)) {
                            Log.e("GLContext", "Cannot swap buffers!");
                        }
                    }
                }
            }
        });
    }

    public EGLSurface createSurface(EGLConfig config, Object surface) {
        if (surface == null) {
            int width = 1;
            int height = 1;
            if (reference != null) {
                CanvasView view = reference.get();
                if (view != null) {
                    width = view.getWidth();
                    height = view.getHeight();
                }
            }
            int[] surfaceAttribs = {
                    EGL10.EGL_WIDTH, width,
                    EGL10.EGL_HEIGHT, height,
                    EGL10.EGL_NONE
            };
            return mEGL.eglCreatePbufferSurface(mEGLDisplay, config, surfaceAttribs);
        }

        return mEGL.eglCreateWindowSurface(mEGLDisplay, config, surface, null);
    }

    public void onPause() {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                mEGL.eglMakeCurrent(mEGLDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
                mGLThread.setPaused(true);
            }
        });
    }

    public void onResume() {
        mGLThread.setPaused(false);
        //  mGLThread.makeEGLContextCurrent();
    }

    public boolean makeCurrent(EGLSurface surface) {
        return mEGL.eglMakeCurrent(mEGLDisplay, surface, surface, mEGLContext);
    }

    public boolean destroySurface(EGLSurface surface) {
        return mEGL.eglDestroySurface(mEGLDisplay, surface);
    }

    public boolean swapBuffers(EGLSurface surface) {
        return mEGL.eglSwapBuffers(mEGLDisplay, surface);
    }

    public boolean isGLThreadStarted() {
        if (mGLThread == null) {
            return false;
        }
        return mGLThread.isStarted;
    }

    public void destroy() {
        if (mGLThread != null) {
            try {
                mGLThread.interrupt();
                mGLThread.join();
            } catch (InterruptedException e) {
                Log.e("GLContext", "Can't interrupt GL thread.", e);
            }
            mGLThread = null;
        }
    }

    private class GLThread extends Thread {
        private boolean isStarted = false;
        private boolean isPaused = false;

        public synchronized void setPaused(boolean paused) {
            isPaused = paused;
        }

        @Override
        public synchronized void start() {
            super.start();
            isStarted = true;
        }

        @Override
        public void interrupt() {
            super.interrupt();
            isStarted = false;
        }


        private Object mSurface;

        private static final int EGL_CONTEXT_CLIENT_VERSION = 0x3098;
        private static final int EGL_CONTEXT_CLIENT_MINOR_VERSION = 0x30FB;

        public GLThread(Object texture) {
            mSurface = texture;
        }

        private void initEGL() {
            mEGL = (EGL10) EGLContext.getEGL();
            mEGLDisplay = mEGL.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
            if (mEGLDisplay == EGL10.EGL_NO_DISPLAY) {
                throw new RuntimeException("eglGetDisplay failed " + GLUtils.getEGLErrorString(mEGL.eglGetError()));
            }

            int[] version = new int[2];
            if (!mEGL.eglInitialize(mEGLDisplay, version)) {
                throw new RuntimeException("eglInitialize failed " + GLUtils.getEGLErrorString(mEGL.eglGetError()));
            }

            // Find a compatible EGLConfig
            int[] configsCount = new int[1];
            EGLConfig[] configs = new EGLConfig[1];
            int[] configSpec = {
                    EGL10.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                    EGL10.EGL_RED_SIZE, 8, EGL10.EGL_GREEN_SIZE, 8, EGL10.EGL_BLUE_SIZE, 8,
                    EGL10.EGL_ALPHA_SIZE, 8, EGL10.EGL_DEPTH_SIZE, 16, EGL10.EGL_STENCIL_SIZE, 0,
                    EGL10.EGL_NONE,
            };
            if (!mEGL.eglChooseConfig(mEGLDisplay, configSpec, configs, 1, configsCount)) {
                throw new IllegalArgumentException("eglChooseConfig failed " + GLUtils.getEGLErrorString(mEGL.eglGetError()));
            } else if (configsCount[0] > 0) {
                mEGLConfig = configs[0];
            }
            if (mEGLConfig == null) {
                throw new RuntimeException("eglConfig not initialized");
            }

            // Create EGLContext and EGLSurface
            mEGLContext = createGLContext(3, 1, mEGLConfig);

            if (mEGLContext == null || mEGLContext == EGL10.EGL_NO_CONTEXT) {
                mEGLContext = createGLContext(3, 0, mEGLConfig);
            }
            if (mEGLContext == null || mEGLContext == EGL10.EGL_NO_CONTEXT) {
                mEGLContext = createGLContext(2, 0, mEGLConfig);
            }


            mEGLSurface = createSurface(mEGLConfig, mSurface);

            if (mEGLSurface == null || mEGLSurface == EGL10.EGL_NO_SURFACE) {
                int error = mEGL.eglGetError();
                throw new RuntimeException("eglCreateWindowSurface failed " + GLUtils.getEGLErrorString(error));
            }

            // Switch to our EGLContext
            makeEGLContextCurrent();

            EGL14.eglSwapInterval(EGL14.eglGetCurrentDisplay(), 0);

            // Enable buffer preservation -- allows app to draw over previous frames without clearing
            /*EGL14.eglSurfaceAttrib(EGL14.eglGetCurrentDisplay(), EGL14.eglGetCurrentSurface(EGL14.EGL_DRAW),
                    EGL14.EGL_SWAP_BEHAVIOR, EGL14.EGL_BUFFER_PRESERVED);*/

          //  GLES20.glClearColor(0, 0, 0, 0);
          //  GLES20.glClearDepthf(1);
          //  GLES20.glClearStencil(0);
           // GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_STENCIL_BUFFER_BIT);

        }

        private void deInitEGL() {
            makeEGLContextCurrent();
            destroySurface(mEGLSurface);
            mEGL.eglDestroyContext(mEGLDisplay, mEGLContext);
            mEGL.eglTerminate(mEGLDisplay);
        }

        private EGLContext createGLContext(int contextVersion, int minorVersion, EGLConfig eglConfig) {
            int[] attribs = {EGL_CONTEXT_CLIENT_VERSION, contextVersion, EGL_CONTEXT_CLIENT_MINOR_VERSION, minorVersion,  EGL10.EGL_NONE};
            return mEGL.eglCreateContext(mEGLDisplay, eglConfig, EGL10.EGL_NO_CONTEXT, attribs);
        }

        private void makeEGLContextCurrent() {
            if (!mEGLContext.equals(mEGL.eglGetCurrentContext()) ||
                    !mEGLSurface.equals(mEGL.eglGetCurrentSurface(EGL10.EGL_DRAW))) {
                if (!makeCurrent(mEGLSurface)) {
                    throw new RuntimeException("eglMakeCurrent failed " + GLUtils.getEGLErrorString(mEGL.eglGetError()));
                }
            }
        }

        @Override
        public void run() {
            initEGL();
            while (true) {
                try {
                    if (!isPaused) {
                        makeEGLContextCurrent();
                        mQueue.take().run();
                    }
                } catch (InterruptedException e) {
                    break;
                }
            }
            deInitEGL();
        }
    }
}
