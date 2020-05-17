package com.github.triniwiz.canvas;

/**
 * Created by triniwiz on 5/15/20
 */
public class TextDecoder {
    private long nativeDecoder = 0;

    private static native long nativeInit(String encoding);

    private static native String nativeGetEncoding(long encoder);

    private static native String nativeDecode(long encoder, byte[] bytes);

    public TextDecoder() {
        init("utf-8");
    }

    public TextDecoder(String encoding) {
        init(encoding);
    }

    private void init(String encoding) {
        nativeDecoder = nativeInit(encoding);
    }

    public String decode(byte[] bytes) {
        return nativeDecode(nativeDecoder, bytes);
    }

    public String getEncoding() {
        return nativeGetEncoding(nativeDecoder);
    }
}
