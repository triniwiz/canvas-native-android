package com.github.triniwiz.canvas;

import android.content.Context;
import android.graphics.*;
import android.opengl.*;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Choreographer;
import androidx.annotation.Nullable;

import javax.microedition.khronos.opengles.GL10;
import java.nio.IntBuffer;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by triniwiz on 2019-07-06
 */
public class CanvasView extends GLSurfaceView implements GLSurfaceView.Renderer, Choreographer.FrameCallback {

    private static native long nativeInit(long canvas_ptr, int id, int width, int height, float scale);

    private static native long nativeDeInit(long canvas_ptr);
    private HandlerThread handlerThread = new HandlerThread("CanvasViewThread");
    private Handler handler;
    static {
        System.loadLibrary("canvasnative");
    }

    long canvas = 0;

    CanvasRenderingContext renderingContext2d = null;

    float scale = 0;
    Context ctx;

    boolean pendingInvalidate;
    static final Object lock = new Object();
    long lastCall = 0;
    @Override
    public void doFrame(long frameTimeNanos) {
        synchronized (lock) {
            final long dt = frameTimeNanos - lastCall;
            if (pendingInvalidate) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        requestRender();
                        pendingInvalidate = false;
                    }
                });
            }
            lastCall = frameTimeNanos;
        }
        Choreographer.getInstance().postFrameCallback(this);
    }

    public CanvasView(Context context) {
        super(context, null);
    }

    public CanvasView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
        ctx = context;
        scale = context.getResources().getDisplayMetrics().density;
        setEGLConfigChooser(8, 8, 8, 8, 24, 0);
        setEGLContextClientVersion(2);
        setPreserveEGLContextOnPause(true);
        getHolder().setFormat(PixelFormat.RGBA_8888);
        setRenderer(this);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        setZOrderOnTop(true);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Choreographer.getInstance().removeFrameCallback(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Choreographer.getInstance().postFrameCallback(this);
    }

    public @Nullable
    CanvasRenderingContext getContext(String type) {
        if (type.equals("2d")) {
            if (renderingContext2d == null) {
                renderingContext2d = new CanvasRenderingContext2D(this);
            }
            return renderingContext2d;
        }
        return null;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, javax.microedition.khronos.egl.EGLConfig config) {
        GLES20.glClearColor(0F, 0F, 0F, 0F);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_STENCIL_BUFFER_BIT);
    }

    int mWidth = -1;
    int mHeight = -1;

    @Override
    public void onSurfaceChanged(GL10 gl, final int width, final int height) {
        if (width != mWidth && height != mHeight) {
            // GLES20.glViewport(0, 0, width, height);
            mWidth = width;
            mHeight = height;
        }
        if (canvas == 0) {
            queueEvent(new Runnable() {
                @Override
                public void run() {
                    canvas = CanvasView.nativeInit(canvas, 0, width, height, scale);
                }
            });
        }
    }


    @Override
    public void onDrawFrame(GL10 gl) {
    }
}
