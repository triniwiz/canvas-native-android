package com.github.triniwiz.canvas;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.util.Log;
import android.view.TextureView;

/**
 * Created by triniwiz on 6/9/20
 */
public class GLView extends TextureView implements TextureView.SurfaceTextureListener {

    private boolean isCreated = false;
    private boolean isCreatedWithZeroSized = false;
    private GLContext mGLContext;
    private CanvasView.Listener mListener;

    public GLView(Context context) {
        super(context);
        init();
    }

    public GLView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    void init() {
        setOpaque(false);
        mGLContext = new GLContext();
        setSurfaceTextureListener(this);
    }

    public void flush() {
        mGLContext.flush();
    }

    public GLContext getGLContext() {
        return mGLContext;
    }

    public void queueEvent(Runnable runnable) {
        mGLContext.queueEvent(runnable);
    }

    public void setListener(CanvasView.Listener listener) {
        this.mListener = listener;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        if (!isCreated) {
            if (width == 0 || height == 0) {
                isCreatedWithZeroSized = true;
            }
            if (!isCreatedWithZeroSized) {
                mGLContext.init(surface);
                if (mListener != null) {
                    mListener.contextReady();
                }
            }
            isCreated = true;
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        if (isCreatedWithZeroSized && (width != 0 || height != 0)) {
            mGLContext.init(surface);
            isCreatedWithZeroSized = false;
            if (mListener != null) {
                mListener.contextReady();
            }
        }
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        isCreated = false;
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

}
