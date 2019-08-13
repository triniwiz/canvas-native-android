package com.github.triniwiz.canvas;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;


/**
 * Created by triniwiz on 2019-07-06
 */
public class CanvasRenderingContext2D implements CanvasRenderingContext {

    private boolean imageSmoothingEnabled = false;
    private ImageSmoothingQuality imageSmoothingQuality = ImageSmoothingQuality.Low;

    private static native long nativeRect(long canvas_ptr, float x, float y, float width, float height);

    private static native long nativeStroke(long canvas_ptr);

    private static native long nativeFill(long canvas_ptr);

    private static native long nativeFillRect(long canvas_ptr, float x, float y, float width, float height);

    private static native long nativeStrokeRect(long canvas_ptr, float x, float y, float width, float height);

    private static native long nativeFillText(long canvas_ptr, String text, float x, float y, float width);

    private static native long nativeStrokeText(long canvas_ptr, String text, float x, float y, float width);

    private static native long nativeBeginPath(long canvas_ptr);

    private static native long nativeMoveTo(long canvas_ptr, float x, float y);

    private static native long nativeLineTo(long canvas_ptr, float x, float y);

    private static native long nativeClosePath(long canvas_ptr);

    private static native long nativeArc(long canvas_ptr, float x, float y, float radius, float startAngle, float endAngle, boolean anticlockwise);

    private static native long nativeArcTo(long canvas_ptr, float x1, float y1, float x2, float y2, float radius);

    private static native long nativeBezierCurveTo(long canvas_ptr, float cp1x, float cp1y, float cp2x, float cp2y, float x, float y);

    private static native long nativeEllipse(long canvas_ptr, float x, float y, float radiusX, float radiusY, float rotation, float startAngle, float endAngle, boolean anticlockwise);

    private static native long nativeClip(long canvas_ptr, String rule);

    private static native long nativeSetLineWidth(long canvas_ptr, float lineWidth);

    private static native long nativeSetGlobalCompositeOperation(long canvas_ptr, String composite);

    private static native long nativeSetTextAlignment(long canvas_ptr, String alignment);

    private static native long nativeSave(long canvas_ptr);

    private static native long nativeRestore(long canvas_ptr);

    private static native long nativeSetTransform(long canvas_ptr, float a, float b, float c, float d, float e, float f);

    private static native long nativeTransform(long canvas, float a, float b, float c, float d, float e, float f);

    private static native long nativeScale(long canvas, float x, float y);

    private static native long nativeRotate(long canvas, float angle);

    private static native long nativeTranslate(long canvas, float x, float y);

    private static native long nativeQuadraticCurveTo(long canvas, float cpx, float cpy, float x, float y);

    private static native long nativeDrawImage(long canvas, Bitmap image, float dx, float dy);

    private static native long nativeDrawImageDw(long canvas, Bitmap image, float dx, float dy, float dWidth, float dHeight);

    private static native long nativeDrawImageSw(long canvas, Bitmap image, float sx, float sy, float sWidth, float sHeight, float dx, float dy, float dWidth, float dHeight);

    private static native long nativeClearRect(long canvas_ptr, float x, float y, float width, float height);

    private static native long nativeSetFillColorRgba(long canvas_ptr, int r, int g, int b, int a);

    private static native long nativeSetStrokeColorRgba(long canvas_ptr, int r, int g, int b, int a);

    private static native long nativeSetShadowBlur(long canvas, float blur);

    private static native long nativeSetShadowColor(long canvas, int color);

    private static native long nativeSetShadowOffsetX(long canvas, float x);

    private static native long nativeSetShadowOffsetY(long canvas, float y);

    private static native long nativeSetFont(long canvas, String font);

    private static native byte[] nativeCreateImageData(int width, int height);

    private static native long nativePutImageData(long canvas, int width, int height, byte[] data, float x, float y, float dirtyX, float dirtyY, int dirtyWidth, int dirtyHeight);

    private static native long nativeSetImageSmoothingEnabled(long canvas, boolean enabled);

    private static native long nativeSetImageSmoothingQuality(long canvas, String quality);

    private static native long nativeSetLineCap(long canvas, String toString);

    private static native long nativeSetFillGradientRadial(long canvas, float x0, float y0, float r0, float x1, float y1, float r1, int[] rawValues, float[] rawKeys);

    private static native long nativeSetFillGradientLinear(long canvas, float x0, float y0, float x1, float y1, int[] rawValues, float[] rawKeys);

    private static native long nativeSetStrokeGradientRadial(long canvas, float x0, float y0, float r0, float x1, float y1, float r1, int[] rawValues, float[] rawKeys);

    private static native long nativeSetStrokeGradientLinear(long canvas, float x0, float y0, float x1, float y1, int[] rawValues, float[] rawKeys);

    private static native long nativeSetLineDashOffset(long canvas, float offset);

    private static native long nativeSetLineDash(long canvas, float[] dash);

    private static native long nativeResetTransform(long canvas);

    private static native CanvasTextMetrics nativeMeasureText(long canvas, String text);

    private CanvasView canvasView;

    CanvasRenderingContext2D(CanvasView view) {
        this.canvasView = view;
    }

    public enum LineCap {
        Butt("butt"),
        Round("round"),
        Square("square");
        private String lineCap;

        LineCap(String lineCap) {
            this.lineCap = lineCap;
        }

        @Override
        public String toString() {
            return lineCap;
        }
    }

    public enum ImageSmoothingQuality {
        Low("low"),
        Medium("medium"),
        High("high");

        private String quality;

        ImageSmoothingQuality(String quality) {
            this.quality = quality;
        }

        @Override
        public String toString() {
            return quality;
        }
    }

    final Object lock = new Object();
    long state = 0;
    long currentPath = 0;
    private float lineWidth = 1;
    private ICanvasColorStyle fillStyle = new CanvasColorStyle.Color(Color.BLACK);
    private ICanvasColorStyle strokeStyle = new CanvasColorStyle.Color(Color.BLACK);
    private CanvasCompositeOperationType globalCompositeOperation = CanvasCompositeOperationType.SourceOver;
    private CanvasTextAlignment textAlign = CanvasTextAlignment.Start;
    float globalAlpha = 1;
    String font = "10px sans-serif";
    LineCap lineCap = LineCap.Butt;
    float lineDashOffset = 0f;

    public ICanvasColorStyle getFillStyle() {
        return fillStyle;
    }

    private void setFillStyleInternal(ICanvasColorStyle fillStyle) {
        this.fillStyle = fillStyle;
    }

    private void setStrokeStyleInternal(ICanvasColorStyle strokeStyle) {
        this.strokeStyle = strokeStyle;
    }

    public void setFillStyle(final ICanvasColorStyle fillStyle) {
        canvasView.queueEvent(new Runnable() {
            @Override
            public void run() {
                switch (fillStyle.getStyleType()) {
                    case Color:
                        CanvasColorStyle.Color color = (CanvasColorStyle.Color) fillStyle;
                        CanvasRenderingContext2D.nativeSetFillColorRgba(canvasView.canvas, Color.red(color.color), Color.green(color.color), Color.blue(color.color), Color.alpha(color.color));
                        break;
                    case Pattern:
                        break;
                    case Gradient:
                        CanvasColorStyle.Gradient gradient = (CanvasColorStyle.Gradient) fillStyle;
                        if (gradient instanceof CanvasColorStyle.LinearGradient) {
                            CanvasColorStyle.LinearGradient g = (CanvasColorStyle.LinearGradient) gradient;
                            canvasView.canvas = CanvasRenderingContext2D.nativeSetFillGradientLinear(canvasView.canvas, g.x0, g.y0, g.x1, g.y1, gradient.getColors(), gradient.getPositions());
                        } else if (gradient instanceof CanvasColorStyle.RadialGradient) {
                            CanvasColorStyle.RadialGradient g = (CanvasColorStyle.RadialGradient) gradient;
                            canvasView.canvas = CanvasRenderingContext2D.nativeSetFillGradientRadial(canvasView.canvas, g.x0, g.y0, g.r0, g.x1, g.y1, g.r1, gradient.getColors(), gradient.getPositions());
                        }
                        break;
                }
                setFillStyleInternal(fillStyle);

            }
        });
    }

    public ICanvasColorStyle getStrokeStyle() {
        return strokeStyle;
    }

    public float getLineWidth() {
        return lineWidth;
    }

    public void setLineWidth(final float lineWidth) {
        canvasView.queueEvent(new Runnable() {
            @Override
            public void run() {
                canvasView.canvas = CanvasRenderingContext2D.nativeSetLineWidth(canvasView.canvas, lineWidth);
                setLineWidthInternal(lineWidth);
            }
        });
    }

    private void setLineWidthInternal(float lineWidth) {
        this.lineWidth = lineWidth;
    }

    private void setLineCapInternal(final LineCap lineCap) {
        this.lineCap = lineCap;
    }

    public void setLineCap(final LineCap lineCap) {
        canvasView.queueEvent(new Runnable() {
            @Override
            public void run() {
                canvasView.canvas = CanvasRenderingContext2D.nativeSetLineCap(canvasView.canvas, lineCap.toString());
                setLineCapInternal(lineCap);
            }
        });
    }

    private void setLineDashOffsetInternal(float offset) {
        this.lineDashOffset = offset;
    }

    public void setLineDashOffset(final float offset) {
        canvasView.queueEvent(new Runnable() {
            @Override
            public void run() {
                canvasView.canvas = CanvasRenderingContext2D.nativeSetLineDashOffset(canvasView.canvas, offset);
                setLineDashOffset(offset);
            }
        });
    }

    private float[] lineDash = new float[0];

    public void setLineDashInternal(final float[] dash) {
        lineDash = dash;
    }

    public float[] getLineDash() {
        return lineDash;
    }

    public void setLineDash(final float[] dash) {
        canvasView.queueEvent(new Runnable() {
            @Override
            public void run() {
                canvasView.canvas = CanvasRenderingContext2D.nativeSetLineDash(canvasView.canvas, dash);
                setLineDashInternal(dash);
            }
        });
    }

    public void setStrokeStyle(final ICanvasColorStyle strokeStyle) {
        canvasView.queueEvent(new Runnable() {
            @Override
            public void run() {
                switch (strokeStyle.getStyleType()) {
                    case Color:
                        CanvasColorStyle.Color color = (CanvasColorStyle.Color) strokeStyle;
                        CanvasRenderingContext2D.nativeSetStrokeColorRgba(canvasView.canvas, Color.red(color.color), Color.green(color.color), Color.blue(color.color), Color.alpha(color.color));
                        break;
                    case Pattern:
                        break;
                    case Gradient:
                        CanvasColorStyle.Gradient gradient = (CanvasColorStyle.Gradient) strokeStyle;
                        if (gradient instanceof CanvasColorStyle.LinearGradient) {
                            CanvasColorStyle.LinearGradient g = (CanvasColorStyle.LinearGradient) gradient;
                            canvasView.canvas = CanvasRenderingContext2D.nativeSetStrokeGradientLinear(canvasView.canvas, g.x0, g.y0, g.x1, g.y1, gradient.getColors(), gradient.getPositions());
                        } else if (gradient instanceof CanvasColorStyle.RadialGradient) {
                            CanvasColorStyle.RadialGradient g = (CanvasColorStyle.RadialGradient) gradient;
                            canvasView.canvas = CanvasRenderingContext2D.nativeSetStrokeGradientRadial(canvasView.canvas, g.x0, g.y0, g.r0, g.x1, g.y1, g.r1, gradient.getColors(), gradient.getPositions());
                        }
                        break;
                }
                setStrokeStyleInternal(strokeStyle);
            }
        });
    }

    Canvas canvas;

    private void updateCanvas() {
        synchronized (CanvasView.lock) {
            canvasView.pendingInvalidate = true;
        }
    }

    public void clearRect(final float x, final float y, final float width, final float height) {
        canvasView.queueEvent(new Runnable() {
            @Override
            public void run() {
                canvasView.canvas = CanvasRenderingContext2D.nativeClearRect(canvasView.canvas, x, y, width, height);
                updateCanvas();
            }
        });
    }

    public void fillRect(final float x, final float y, final float width, final float height) {
        canvasView.queueEvent(new Runnable() {
            @Override
            public void run() {
                canvasView.canvas = CanvasRenderingContext2D.nativeFillRect(canvasView.canvas, x, y, width, height);
                updateCanvas();
            }
        });
    }

    public void strokeRect(final float x, final float y, final float width, final float height) {
        canvasView.queueEvent(new Runnable() {
            @Override
            public void run() {
                canvasView.canvas = CanvasRenderingContext2D.nativeStrokeRect(canvasView.canvas, x, y, width, height);
                updateCanvas();
            }
        });
    }

    public void fillText(String text, float x, float y) {
        fillText(text, x, y, 0);
    }

    public void fillText(final String text, final float x, final float y, final float width) {
        canvasView.queueEvent(new Runnable() {
            @Override
            public void run() {
                canvasView.canvas = CanvasRenderingContext2D.nativeFillText(canvasView.canvas, text, x, y, width);
                updateCanvas();
            }
        });
    }

    public void strokeText(String text, float x, float y) {
        strokeText(text, x, y, 0);
    }

    public void strokeText(final String text, final float x, final float y, final float width) {
        canvasView.queueEvent(new Runnable() {
            @Override
            public void run() {
                canvasView.canvas = CanvasRenderingContext2D.nativeStrokeText(canvasView.canvas, text, x, y, width);
                updateCanvas();
            }
        });
    }

    public void rect(final float x, final float y, final float width, final float height) {
        canvasView.queueEvent(new Runnable() {
            @Override
            public void run() {
                canvasView.canvas = CanvasRenderingContext2D.nativeRect(canvasView.canvas, x, y, width, height);
            }
        });
    }

    public void fill() {
        canvasView.queueEvent(new Runnable() {
            @Override
            public void run() {
                canvasView.canvas = CanvasRenderingContext2D.nativeFill(canvasView.canvas);
                updateCanvas();
            }
        });
    }

    public void stroke() {
        canvasView.queueEvent(new Runnable() {
            @Override
            public void run() {
                canvasView.canvas = CanvasRenderingContext2D.nativeStroke(canvasView.canvas);
                updateCanvas();
            }
        });
    }

    public void beginPath() {
        canvasView.queueEvent(new Runnable() {
            @Override
            public void run() {
                canvasView.canvas = CanvasRenderingContext2D.nativeBeginPath(canvasView.canvas);
            }
        });
    }

    public void moveTo(final float x, final float y) {
        canvasView.queueEvent(new Runnable() {
            @Override
            public void run() {
                canvasView.canvas = CanvasRenderingContext2D.nativeMoveTo(canvasView.canvas, x, y);
            }
        });
    }

    public void lineTo(final float x, final float y) {
        canvasView.queueEvent(new Runnable() {
            @Override
            public void run() {
                canvasView.canvas = CanvasRenderingContext2D.nativeLineTo(canvasView.canvas, x, y);
            }
        });
    }

    public void closePath() {
        canvasView.queueEvent(new Runnable() {
            @Override
            public void run() {
                canvasView.canvas = CanvasRenderingContext2D.nativeClosePath(canvasView.canvas);
            }
        });
    }

    public void arc(float x, float y, float radius, float startAngle, float endAngle) {
        arc(x, y, radius, startAngle, endAngle, false);
    }

    public void arc(final float x, final float y, final float radius, final float startAngle, final float endAngle, final boolean anticlockwise) {
        canvasView.queueEvent(new Runnable() {
            @Override
            public void run() {
                canvasView.canvas = CanvasRenderingContext2D.nativeArc(canvasView.canvas, x, y, radius, startAngle, endAngle, anticlockwise);
            }
        });
    }

    public void arcTo(final float x1, final float y1, final float x2, final float y2, final float radius) {
        canvasView.queueEvent(new Runnable() {
            @Override
            public void run() {
                canvasView.canvas = CanvasRenderingContext2D.nativeArcTo(canvasView.canvas, x1, y1, x2, y2, radius);
            }
        });
    }

    public void bezierCurveTo(final float cp1x, final float cp1y, final float cp2x, final float cp2y, final float x, final float y) {
        canvasView.queueEvent(new Runnable() {
            @Override
            public void run() {
                canvasView.canvas = CanvasRenderingContext2D.nativeBezierCurveTo(canvasView.canvas, cp1x, cp1y, cp2x, cp2y, x, y);
            }
        });
    }

    public void ellipse(float x, float y, float radiusX, float radiusY, float rotation, float startAngle, float endAngle) {
        ellipse(x, y, radiusX, radiusY, rotation, startAngle, endAngle, false);
    }

    public void ellipse(final float x, final float y, final float radiusX, final float radiusY, final float rotation, final float startAngle, final float endAngle, final boolean anticlockwise) {
        canvasView.queueEvent(new Runnable() {
            @Override
            public void run() {
                canvasView.canvas = CanvasRenderingContext2D.nativeEllipse(canvasView.canvas, x, y, radiusX, radiusY, rotation, startAngle, endAngle, anticlockwise);
            }
        });
    }

    public void clip() {
        clip("nonzero");
    }

    public void clip(final String rule) {
        canvasView.queueEvent(new Runnable() {
            @Override
            public void run() {
                canvasView.canvas = CanvasRenderingContext2D.nativeClip(canvasView.canvas, rule);
                updateCanvas();
            }
        });
    }

    public CanvasView getCanvas() {
        return canvasView;
    }

    public CanvasColorStyle.LinearGradient createLinearGradient(float x0, float y0, float x1, float y1) {
        return new CanvasColorStyle.LinearGradient(x0, y0, x1, y1);
    }

    public CanvasColorStyle.RadialGradient createRadialGradient(float x0, float y0, float r0, float x1, float y1, float r1) {
        return new CanvasColorStyle.RadialGradient(x0, y0, r0, x1, y1, r1);
    }

    public CanvasColorStyle.Pattern createPattern(Object src, CanvasColorStyle.Pattern.PatternRepetition repetition) {
        return new CanvasColorStyle.Pattern(src, repetition);
    }

    public CanvasCompositeOperationType getGlobalCompositeOperation() {
        return globalCompositeOperation;
    }

    private void setGlobalCompositeOperationInternal(CanvasCompositeOperationType globalCompositeOperation) {
        this.globalCompositeOperation = globalCompositeOperation;
    }

    public void setGlobalCompositeOperation(final CanvasCompositeOperationType globalCompositeOperation) {
        canvasView.queueEvent(new Runnable() {
            @Override
            public void run() {
                canvasView.canvas = CanvasRenderingContext2D.nativeSetGlobalCompositeOperation(canvasView.canvas, globalCompositeOperation.type);
                setGlobalCompositeOperationInternal(globalCompositeOperation);
            }
        });
    }

    public CanvasTextAlignment getTextAlign() {
        return textAlign;
    }

    private void setTextAlignInternal(final CanvasTextAlignment textAlign) {
        this.textAlign = textAlign;
    }

    public void setTextAlign(final CanvasTextAlignment textAlign) {
        canvasView.queueEvent(new Runnable() {
            @Override
            public void run() {
                canvasView.canvas = CanvasRenderingContext2D.nativeSetTextAlignment(canvasView.canvas, textAlign.toString());
                setTextAlignInternal(textAlign);
            }
        });
    }

    public void save() {
        canvasView.queueEvent(new Runnable() {
            @Override
            public void run() {
                canvasView.canvas = CanvasRenderingContext2D.nativeSave(canvasView.canvas);
                updateCanvas();
            }
        });
    }

    public void restore() {
        canvasView.queueEvent(new Runnable() {
            @Override
            public void run() {
                canvasView.canvas = CanvasRenderingContext2D.nativeRestore(canvasView.canvas);
                updateCanvas();
            }
        });
    }


    public void setTransform(final float a, final float b, final float c, final float d, final float e, final float f) {
        canvasView.queueEvent(new Runnable() {
            @Override
            public void run() {
                canvasView.canvas = CanvasRenderingContext2D.nativeSetTransform(canvasView.canvas, a, b, c, d, e, f);
                updateCanvas();
            }
        });
    }

    public void transform(final float a, final float b, final float c, final float d, final float e, final float f) {
        canvasView.queueEvent(new Runnable() {
            @Override
            public void run() {
                canvasView.canvas = CanvasRenderingContext2D.nativeTransform(canvasView.canvas, a, b, c, d, e, f);
                updateCanvas();
            }
        });
    }

    public void scale(final float x, final float y) {
        canvasView.queueEvent(new Runnable() {
            @Override
            public void run() {
                canvasView.canvas = CanvasRenderingContext2D.nativeScale(canvasView.canvas, x, y);
                updateCanvas();
            }
        });
    }

    public void rotate(final float angle) {
        canvasView.queueEvent(new Runnable() {
            @Override
            public void run() {
                canvasView.canvas = CanvasRenderingContext2D.nativeRotate(canvasView.canvas, angle);
                updateCanvas();
            }
        });
    }

    public void translate(final float x, final float y) {
        canvasView.queueEvent(new Runnable() {
            @Override
            public void run() {
                canvasView.canvas = CanvasRenderingContext2D.nativeTranslate(canvasView.canvas, x, y);
                updateCanvas();
            }
        });
    }

    public void quadraticCurveTo(final float cpx, final float cpy, final float x, final float y) {
        canvasView.queueEvent(new Runnable() {
            @Override
            public void run() {
                canvasView.canvas = CanvasRenderingContext2D.nativeQuadraticCurveTo(canvasView.canvas, cpx, cpy, x, y);
            }
        });
    }

    public void drawImage(final Bitmap image, final float dx, final float dy) {
        canvasView.queueEvent(new Runnable() {
            @Override
            public void run() {
                canvasView.canvas = CanvasRenderingContext2D.nativeDrawImage(canvasView.canvas, image, dx, dy);
                updateCanvas();
            }
        });
    }

    public void drawImage(final Bitmap image, final float dx, final float dy, final float dWidth, final float dHeight) {
        canvasView.queueEvent(new Runnable() {
            @Override
            public void run() {
                canvasView.canvas = CanvasRenderingContext2D.nativeDrawImageDw(canvasView.canvas, image, dx, dy, dWidth, dHeight);
                updateCanvas();
            }
        });
    }

    public void drawImage(final Bitmap image, final float sx, final float sy, final float sWidth, final float sHeight, final float dx, final float dy, final float dWidth, final float dHeight) {
        canvasView.queueEvent(new Runnable() {
            @Override
            public void run() {
                canvasView.canvas = CanvasRenderingContext2D.nativeDrawImageSw(canvasView.canvas, image, sx, sy, sWidth, sHeight, dx, dy, dWidth, dHeight);
                updateCanvas();
            }
        });
    }

    public void setShadowBlur(final float blur) {
        canvasView.queueEvent(new Runnable() {
            @Override
            public void run() {
                canvasView.canvas = CanvasRenderingContext2D.nativeSetShadowBlur(canvasView.canvas, blur);
            }
        });
    }

    public float getShadowBlur() {
        return 0;
    }

    public void setShadowColor(final int color) {
        canvasView.queueEvent(new Runnable() {
            @Override
            public void run() {
                canvasView.canvas = CanvasRenderingContext2D.nativeSetShadowColor(canvasView.canvas, color);
            }
        });
    }

    public int getShadowColor() {
        return 0;
    }

    public void setShadowOffsetX(final float x) {
        canvasView.queueEvent(new Runnable() {
            @Override
            public void run() {
                canvasView.canvas = CanvasRenderingContext2D.nativeSetShadowOffsetX(canvasView.canvas, x);
            }
        });
    }

    public float getShadowOffsetX() {
        return 0;
    }

    public void setShadowOffsetY(final float y) {
        canvasView.queueEvent(new Runnable() {
            @Override
            public void run() {
                canvasView.canvas = CanvasRenderingContext2D.nativeSetShadowOffsetY(canvasView.canvas, y);
            }
        });
    }

    public float getShadowOffsetY() {
        return 0;
    }

    public void setFont(final String font) {
        canvasView.queueEvent(new Runnable() {
            @Override
            public void run() {
                canvasView.canvas = CanvasRenderingContext2D.nativeSetFont(canvasView.canvas, font);
            }
        });
    }

    public String getFont() {
        return "";
    }

    public CanvasTextMetrics measureText(String text) {
        return CanvasRenderingContext2D.nativeMeasureText(canvasView.canvas, text);
    }

    public ImageData createImageData(int width, int height) {
        byte[] data = CanvasRenderingContext2D.nativeCreateImageData(width, height);
        return new ImageData(width, height, data);
    }

    public ImageData createImageData(ImageData imageData) {
        byte[] data = CanvasRenderingContext2D.nativeCreateImageData(imageData.getWidth(), imageData.getHeight());
        return new ImageData(imageData.getWidth(), imageData.getHeight(), data);
    }

    public void putImageData(ImageData data) {
        putImageData(data, 0, 0);
    }

    public void putImageData(ImageData data, float x, float y) {
        putImageData(data, x, y, 0, 0, -1, -1);
    }

    public void putImageData(final ImageData data, final float x, final float y, final float dirtyX, final float dirtyY, final int dirtyWidth, final int dirtyHeight) {
        canvasView.queueEvent(new Runnable() {
            @Override
            public void run() {
                canvasView.canvas = CanvasRenderingContext2D.nativePutImageData(canvasView.canvas, data.getWidth(), data.getHeight(), data.getData(), x, y, dirtyX, dirtyY, dirtyWidth, dirtyHeight);
                updateCanvas();
            }
        });
    }

    private static native byte[] nativeGetImageData(long canvas, float sx, float sy, int width, int height);

    public ImageData getImageData(float sx, float sy, int sw, int sh) {
        byte[] data = CanvasRenderingContext2D.nativeGetImageData(canvasView.canvas, sx, sy, sw, sh);
        return new ImageData(sw, sh, data);
    }

    public boolean getImageSmoothingEnabled() {
        return imageSmoothingEnabled;
    }

    private void setImageSmoothingEnabledInternal(final boolean enabled) {
        imageSmoothingEnabled = enabled;
    }

    public void setImageSmoothingEnabled(final boolean enabled) {
        canvasView.queueEvent(new Runnable() {
            @Override
            public void run() {
                canvasView.canvas = CanvasRenderingContext2D.nativeSetImageSmoothingEnabled(canvasView.canvas, enabled);
                setImageSmoothingEnabledInternal(enabled);
            }
        });
    }

    public ImageSmoothingQuality getImageSmoothingQuality() {
        return imageSmoothingQuality;
    }

    private void setImageSmoothingQualityInternal(final ImageSmoothingQuality quality) {
        this.imageSmoothingQuality = quality;
    }

    public void setImageSmoothingQuality(final ImageSmoothingQuality quality) {
        canvasView.queueEvent(new Runnable() {
            @Override
            public void run() {
                canvasView.canvas = CanvasRenderingContext2D.nativeSetImageSmoothingQuality(canvasView.canvas, quality.quality);
                setImageSmoothingQualityInternal(quality);
            }
        });
    }

    public void resetTransform() {
        canvasView.queueEvent(new Runnable() {
            @Override
            public void run() {
                canvasView.canvas = CanvasRenderingContext2D.nativeResetTransform(canvasView.canvas);
            }
        });
    }


}
