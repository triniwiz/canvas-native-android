package com.github.triniwiz.canvas;

import android.graphics.Color;

import java.util.*;

/**
 * Created by triniwiz on 2019-07-13
 */
public class CanvasColorStyle {
    public static class Color implements ICanvasColorStyle {
        int color;

        public Color(int color) {
            this.color = color;
        }

        @Override
        public CanvasColorStyleType getStyleType() {
            return CanvasColorStyleType.Color;
        }
    }

    public static class Gradient implements ICanvasColorStyle {
        Map<Float, Integer> gradientMap;
        ArrayList<Integer> colors;
        ArrayList<Float> keys;

        Gradient() {
            this.gradientMap = new HashMap<>();
            this.colors = new ArrayList<>();
            this.keys = new ArrayList<>();
        }

        @Override
        public CanvasColorStyleType getStyleType() {
            return CanvasColorStyleType.Gradient;
        }

        public void addColorStop(float offset, int color) throws Exception {
            if (offset < 0) {
                throw new Exception("INDEX_SIZE_ERR");
            }

            if (offset > 1) {
                throw new Exception("INDEX_SIZE_ERR");
            }
            this.gradientMap.put(offset, color);
            this.colors.add(color);
            this.keys.add(offset);
        }

        int[] getColors() {
            Collection<Integer> valuesCollection = gradientMap.values();
            int[] rawValues = new int[valuesCollection.size()];
            int i = 0;
            for (Integer value : valuesCollection) {
                rawValues[i] = value;
                i++;
            }
            return rawValues;
        }

        float[] getPositions() {
            Set<Float> keysSet = gradientMap.keySet();
            float[] rawKeys = new float[keysSet.size()];

            int i = 0;
            for (Float key : keysSet) {
                rawKeys[i] = key;
                i++;
            }
            return rawKeys;
        }

    }

    public static class LinearGradient extends Gradient {
        float x0;
        float y0;
        float x1;
        float y1;

        public LinearGradient(float x0, float y0, float x1, float y1) {
            this.x0 = x0;
            this.y0 = y0;
            this.x1 = x1;
            this.y1 = y1;
        }
    }

    public static class RadialGradient extends Gradient {
        float x0;
        float y0;
        float r0;
        float x1;
        float y1;
        float r1;

        public RadialGradient(float x0, float y0, float r0, float x1, float y1, float r1) {
            this.x0 = x0;
            this.y0 = y0;
            this.r0 = r0;
            this.r1 = r1;
            this.x1 = x1;
            this.y1 = y1;
        }
    }

    public static class Pattern implements ICanvasColorStyle {
        PatternRepetition repetition;
        Object src;

        public enum PatternRepetition {
            Repeat("repeat"),
            RepeatX("repeat-x"),
            RepeatY("repeat-y"),
            NoRepeat("no-repeat");
            private String pattern;

            PatternRepetition(String pattern) {
                this.pattern = pattern;
            }

            @Override
            public String toString() {
                return pattern;
            }
        }

        public Pattern(Object src, PatternRepetition repetition) {
            this.src = src;
            this.repetition = repetition;
        }

        @Override
        public CanvasColorStyleType getStyleType() {
            return CanvasColorStyleType.Pattern;
        }
    }
}
