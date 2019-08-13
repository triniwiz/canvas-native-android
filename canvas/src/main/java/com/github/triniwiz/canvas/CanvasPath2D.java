package com.github.triniwiz.canvas;

/**
 * Created by triniwiz on 2019-08-11
 */
public class CanvasPath2D {
    long path;

    private static native long nativeInit();

    private static native long nativeInitWithPath(long path);

    private static native long nativeAddPath(long path_ptr, long path1);

    private static native long nativeClosePath(long path_ptr);

    private static native long nativeRect(long path, float x, float y, float width, float height);

    private static native long nativeMoveTo(long path, float x, float y);

    private static native long nativeLineTo(long path_ptr, float x, float y);

    private static native long nativeArc(long path_ptr, float x, float y, float radius, float startAngle, float endAngle, boolean anticlockwise);

    private static native long nativeArcTo(long path_ptr, float x1, float y1, float x2, float y2, float radius);

    private static native long nativeBezierCurveTo(long path_ptr, float cp1x, float cp1y, float cp2x, float cp2y, float x, float y);

    private static native long nativeEllipse(long path_ptr, float x, float y, float radiusX, float radiusY, float rotation, float startAngle, float endAngle, boolean anticlockwise);

    private static native long nativeQuadraticCurveTo(long path_ptr, float cpx, float cpy, float x, float y);


    public CanvasPath2D() {
        path = CanvasPath2D.nativeInit();
    }

    public CanvasPath2D(CanvasPath2D path2D) {
        path = CanvasPath2D.nativeInitWithPath(path2D.path);
    }

    public void addPath(CanvasPath2D path2D) {
        path = CanvasPath2D.nativeAddPath(path, path2D.path);
    }

    public void closePath() {
        path = CanvasPath2D.nativeClosePath(path);
    }

    public void moveTo(float x, float y) {
        path = CanvasPath2D.nativeMoveTo(path, x, y);
    }

    public void rect(float x, float y, float width, float height) {
        nativeRect(path, x, y, width, height);
    }

    public void lineTo(float x, float y) {
        nativeLineTo(path, x, y);
    }

    public void arc(float x, float y, float radius, float startAngle, float endAngle, boolean anticlockwise) {
        nativeArc(path, x, y, radius, startAngle, endAngle, anticlockwise);
    }

    public void arcTo(float x1, float y1, float x2, float y2, float radius) {
        nativeArcTo(path, x1, y1, x2, y2, radius);
    }

    public void bezierCurveTo(float cp1x, float cp1y, float cp2x, float cp2y, float x, float y) {
        nativeBezierCurveTo(path, cp1x, cp1y, cp2x, cp2y, x, y);
    }

    public void ellipse(float x, float y, float radiusX, float radiusY, float rotation, float startAngle, float endAngle, boolean anticlockwise) {
        nativeEllipse(path, x, y, radiusX, radiusY, rotation, startAngle, endAngle, anticlockwise);
    }

    public void quadraticCurveTo(float cpx, float cpy, float x, float y) {
        nativeQuadraticCurveTo(path, cpx, cpy, x, y);
    }

}
