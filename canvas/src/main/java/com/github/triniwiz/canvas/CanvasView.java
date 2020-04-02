package com.github.triniwiz.canvas;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.ColorDrawable;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Size;
import android.view.Choreographer;
import android.view.SurfaceHolder;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.util.concurrent.TimeUnit;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by triniwiz on 3/29/20
 */
public class CanvasView extends FrameLayout implements GLTextureView.Renderer, Choreographer.FrameCallback, SurfaceHolder.Callback, Application.ActivityLifecycleCallbacks {

    private static native long nativeInit(long canvas_ptr, int id, int width, int height, float scale);

    private static native long nativeResize(long canvas_ptr, int id, int width, int height, float scale);

    private static native long nativeRecreate(long canvas_ptr, int id, int width, int height, float scale);

    private static native long nativeDeInit(long canvas_ptr);

    private static native long nativeDestroy(long canvas_ptr);

    private static native long nativeFlush(long canvas_ptr);

    private static native String nativeToDataUrl(long canvas_ptr, String type, float quality);

    private static native byte[] nativeToData(long canvas_ptr);

    private HandlerThread handlerThread = new HandlerThread("CanvasViewThread");
    private Handler handler;

    private GLTextureView glSurfaceView;
    private boolean handleInvalidationManually = false;
    long canvas = 0;
    CanvasRenderingContext renderingContext2d = null;
    float scale = 0;
    Context ctx;
    boolean pendingInvalidate;
    final Object lock = new Object();
    final static long ONE_MILLISECOND_NS = 1000000;
    static final long ONE_S_IN_NS = 1000 * ONE_MILLISECOND_NS;
    Handler mainHandler;
    boolean wasPendingDraw = false;
    static long lastCall = 0;

    void clear() {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
    }

    @Override
    public void doFrame(long frameTimeNanos) {
        if (!handleInvalidationManually) {
            final long dt = TimeUnit.NANOSECONDS.toMillis(frameTimeNanos - lastCall);
            queueEvent(new Runnable() {
                @Override
                public void run() {
                    if (pendingInvalidate) {
                        flush();
                    }
                }
            });
            lastCall = frameTimeNanos;
        }
        Choreographer.getInstance().postFrameCallback(this);
    }

    public CanvasView(Context context) {
        super(context, null);
    }

    private static boolean isLibraryLoaded = false;

    public CanvasView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        if (isInEditMode()) {
            return;
        }
        if (!isLibraryLoaded) {
            System.loadLibrary("canvasnative");
            isLibraryLoaded = true;
        }
        glSurfaceView = new GLTextureView(context, attrs);
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
        mainHandler = new Handler(Looper.getMainLooper());
        ctx = context;
        scale = context.getResources().getDisplayMetrics().density;
        glSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 24, 8);
        if (detectOpenGLES30() && !isEmulator()) {
           glSurfaceView.setEGLContextClientVersion(3);
        } else {
           glSurfaceView.setEGLContextClientVersion(2);
        }

       glSurfaceView.setPreserveEGLContextOnPause(true);
        glSurfaceView.setRenderer(this);
       // glSurfaceView.getHolder().addCallback(this);
       //glSurfaceView.getHolder().setFormat(PixelFormat.TRANSPARENT);
        glSurfaceView.setRenderMode(GLTextureView.RENDERMODE_WHEN_DIRTY);
        //glSurfaceView.setZOrderOnTop(false);
        glSurfaceView.setLayoutParams(
                new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        );

        //  CanvasView.this.setForeground(new ColorDrawable(Color.WHITE));
        addView(glSurfaceView);

    }

    private boolean detectOpenGLES30() {
        ActivityManager am =
                (ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo info = am.getDeviceConfigurationInfo();
        return (info.reqGlEsVersion >= 0x30000);
    }

    public void onPause() {
        Choreographer.getInstance().removeFrameCallback(this);
    }

    public void onResume() {
        Choreographer.getInstance().postFrameCallback(this);
    }

    public void destroy() {
        nativeDestroy(canvas);
        canvas = 0;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (canvas != 0) {
            destroy();
        }
    }

    public void setHandleInvalidationManually(boolean handleInvalidationManually) {
        this.handleInvalidationManually = handleInvalidationManually;
    }

    public boolean isHandleInvalidationManually() {
        return handleInvalidationManually;
    }

    public GLTextureView getSurface() {
        return glSurfaceView;
    }

    public void queueEvent(Runnable runnable) {
        if (glSurfaceView != null) {
            glSurfaceView.queueEvent(runnable);
        }
    }

    public void setupActivityHandler(Application app) {
        app.unregisterActivityLifecycleCallbacks(this);
        app.registerActivityLifecycleCallbacks(this);
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {

    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        onResume();
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {

    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        onPause();
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {

    }

    public interface DataURLListener {
        void onResult(String data);
    }


    public byte[] toData() {
        return nativeToData(canvas);
    }

    public String toDataURL() {
        return toDataURL("image/png");
    }


    public void toDataURLAsync(DataURLListener listener) {
        toDataURLAsync("image/png", listener);
    }

    public void toDataURLAsync(String type, DataURLListener listener) {
        toDataURLAsync(type, 0.92f, listener);
    }

    public void toDataURLAsync(final String type, final float quality, final DataURLListener listener) {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                listener.onResult(nativeToDataUrl(canvas, type, quality));
            }
        });
    }

    public String toDataURL(String type) {
        return toDataURL(type, 0.92f);
    }

    public String toDataURL(String type, float quality) {
        try {
            //Must sleep since the flush is async
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return nativeToDataUrl(canvas, type, quality);
    }


    public static CanvasDOMMatrix createSVGMatrix() {
        return new CanvasDOMMatrix();
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

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, final int width, final int height) {
    }


    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }


    @Override
    public void onSurfaceCreated(GL10 gl, javax.microedition.khronos.egl.EGLConfig config) {
    }

    int mWidth = -1;
    int mHeight = -1;

    int renderCount = 0;
    boolean wasDestroyed = false;

    Size lastSize;
    Size newSize;

    @Override
    public void onSurfaceChanged(GL10 gl, final int width, final int height) {
        GLES20.glViewport(0, 0, width, height);
        newSize = new Size(width, height);
    }

    public void flush() {
        glSurfaceView.requestRender();
    }

    void showForeground() {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                CanvasView.this.setForeground(new ColorDrawable(Color.WHITE));
            }
        });
    }

    void hideForeground() {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                CanvasView.this.setForeground(null);
            }
        });
    }

    @Override
    public void onDrawFrame(GL10 gl) {
            if (canvas == 0) {
               GLES20.glClearColor(1F, 1F, 1F, 1F);
                lastSize = newSize;
                int[] frameBuffers = new int[1];
                GLES20.glGetIntegerv(GLES20.GL_FRAMEBUFFER_BINDING, frameBuffers, 0);
                canvas = CanvasView.nativeInit(canvas, frameBuffers[0], lastSize.getWidth(), lastSize.getHeight(), scale);
            }
            if (lastSize != newSize) {
                lastSize = newSize;
                // TODO fix rotation
               /* int[] frameBuffers = new int[1];
                GLES20.glGetIntegerv(GLES20.GL_FRAMEBUFFER_BINDING, frameBuffers, 0);
                canvas = CanvasView.nativeResize(canvas, frameBuffers[0], lastSize.getWidth(), lastSize.getHeight(), scale);
                */
            }
            if (renderCount < 3) {
                renderCount++;
            }
            if (pendingInvalidate) {
               // clear();
                canvas = nativeFlush(canvas);
                pendingInvalidate = false;
            }

    }


    private static int rating = -1;

    public static boolean isEmulator() {
        int newRating = 0;
        if (rating < 0) {
            if (Build.PRODUCT.contains("sdk") ||
                    Build.PRODUCT.contains("Andy") ||
                    Build.PRODUCT.contains("ttVM_Hdragon") ||
                    Build.PRODUCT.contains("google_sdk") ||
                    Build.PRODUCT.contains("Droid4X") ||
                    Build.PRODUCT.contains("nox") ||
                    Build.PRODUCT.contains("sdk_x86") ||
                    Build.PRODUCT.contains("sdk_google") ||
                    Build.PRODUCT.contains("vbox86p")) {
                newRating++;
            }

            if (Build.MANUFACTURER.equals("unknown") ||
                    Build.MANUFACTURER.equals("Genymotion") ||
                    Build.MANUFACTURER.contains("Andy") ||
                    Build.MANUFACTURER.contains("MIT") ||
                    Build.MANUFACTURER.contains("nox") ||
                    Build.MANUFACTURER.contains("TiantianVM") ||
                    Build.MANUFACTURER.contains("vmos")) {
                newRating++;
            }

            if (Build.BRAND.equals("generic") ||
                    Build.BRAND.equals("generic_x86") ||
                    Build.BRAND.equals("TTVM") ||
                    Build.BRAND.contains("Andy")) {
                newRating++;
            }

            if (Build.DEVICE.contains("generic") ||
                    Build.DEVICE.contains("generic_x86") ||
                    Build.DEVICE.contains("Andy") ||
                    Build.DEVICE.contains("ttVM_Hdragon") ||
                    Build.DEVICE.contains("Droid4X") ||
                    Build.DEVICE.contains("nox") ||
                    Build.DEVICE.contains("generic_x86_64") ||
                    Build.DEVICE.contains("vbox86p")) {
                newRating++;
            }

            if (Build.MODEL.equals("sdk") ||
                    Build.MODEL.equals("google_sdk") ||
                    Build.MODEL.contains("Droid4X") ||
                    Build.MODEL.contains("TiantianVM") ||
                    Build.MODEL.contains("Andy") ||
                    Build.MODEL.equals("Android SDK built for x86_64") ||
                    Build.MODEL.equals("Android SDK built for x86") ||
                    Build.MODEL.equals("vmos")) {
                newRating++;
            }

            if (Build.HARDWARE.equals("goldfish") ||
                    Build.HARDWARE.equals("vbox86") ||
                    Build.HARDWARE.contains("nox") ||
                    Build.HARDWARE.contains("ttVM_x86")) {
                newRating++;
            }

            if (Build.FINGERPRINT.contains("generic/sdk/generic") ||
                    Build.FINGERPRINT.contains("generic_x86/sdk_x86/generic_x86") ||
                    Build.FINGERPRINT.contains("Andy") ||
                    Build.FINGERPRINT.contains("ttVM_Hdragon") ||
                    Build.FINGERPRINT.contains("generic_x86_64") ||
                    Build.FINGERPRINT.contains("generic/google_sdk/generic") ||
                    Build.FINGERPRINT.contains("vbox86p") ||
                    Build.FINGERPRINT.contains("generic/vbox86p/vbox86p") ||
                    Build.FINGERPRINT.contains("test-keys")) {
                newRating++;
            }

            try {
                String opengl = android.opengl.GLES20.glGetString(android.opengl.GLES20.GL_RENDERER);
                if (opengl != null) {
                    if (opengl.contains("Bluestacks") ||
                            opengl.contains("Translator")
                    )
                        newRating += 10;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                File sharedFolder = new File(Environment
                        .getExternalStorageDirectory().toString()
                        + File.separatorChar
                        + "windows"
                        + File.separatorChar
                        + "BstSharedFolder");

                if (sharedFolder.exists()) {
                    newRating += 10;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            rating = newRating;
        }
        return rating > 3;
    }
}
