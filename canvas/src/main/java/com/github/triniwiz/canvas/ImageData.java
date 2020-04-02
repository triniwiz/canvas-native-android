package com.github.triniwiz.canvas;

/**
 * Created by triniwiz on 2019-08-04
 */
public class ImageData {
    private byte[] data;
    private int width;
    private int height;

    ImageData(int width, int height, byte[] data) {
        this.width = width;
        this.height = height;
        this.data = data;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public byte[] getData() {
        return data;
    }

    public void updateData(byte[] data) {
        this.data = null;
        this.data = data;
    }

}
