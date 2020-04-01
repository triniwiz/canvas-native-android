package com.github.triniwiz.canvas;

import android.animation.TimeAnimator;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.Choreographer;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by triniwiz on 2019-08-13
 */
public class AnimationFrame implements Choreographer.FrameCallback {
    private static ConcurrentHashMap<String, Callback> callbacks;
    static long lastCall = 0;
    static int count = 0;
    static Timer timer;
    static AnimationFrame instance;
    static HandlerThread handlerThread;
    static Handler handler;
    static final Object lock = new Object();
    static Choreographer choreographer;

    static {
        callbacks = new ConcurrentHashMap<>();
        instance = new AnimationFrame();
        handlerThread = new HandlerThread("AnimationFrame");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
    }

    @Override
    public void doFrame(long frameTimeNanos) {
        final long dt = TimeUnit.NANOSECONDS.toMillis(frameTimeNanos - lastCall);
        handler.post(new Runnable() {
            @Override
            public void run() {
                Set<Map.Entry<String, Callback>> set = callbacks.entrySet();
                for (final Map.Entry<String, Callback> callback : set) {
                    callback.getValue().onFrame(dt);
                    if (!callback.getKey().equals("main")) {
                        callbacks.remove(callback.getKey());
                    }
                }
            }
        });
        lastCall = frameTimeNanos;
        Choreographer.getInstance().postFrameCallbackDelayed(instance, TimeUnit.MILLISECONDS.toNanos(dt));
    }

    public interface Callback {
        void onFrame(long called);
    }

    public static String requestAnimationFrame(Callback callback) {
        String id = UUID.randomUUID().toString();
        callbacks.put(id, callback);
        if (choreographer == null) {
            Choreographer.getInstance().postFrameCallback(instance);
        }
        return id;
    }

    static void requestAnimationFrame(String id, Callback callback) {
        callbacks.put(id, callback);
    }

    public static void cancelAnimationFrame(String id) {
        callbacks.remove(id);
        if (callbacks.isEmpty()) {
            Choreographer.getInstance().removeFrameCallback(instance);
            choreographer = null;
        }
    }
}
