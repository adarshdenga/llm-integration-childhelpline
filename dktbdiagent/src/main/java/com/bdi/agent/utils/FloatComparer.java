package com.bdi.agent.utils;

public class FloatComparer {
    private final float epsilon;

    public FloatComparer(float epsilon) {
        this.epsilon = epsilon;
    }

    public boolean greaterOrEqual(float a, float b) {
        return b - a < epsilon || Math.abs(a - b) < epsilon;
    }

    public boolean greaterThan(float a, float b) {
        return a - b > epsilon;
    }

    public boolean lessOrEqual(float a, float b) {
        return b - a > epsilon || Math.abs(a - b) < epsilon;
    }

    public boolean lessThan(float a, float b) {
        return b - a > epsilon;
    }

    public boolean equalTo(float a, float b) {
        return Math.abs(a - b) < epsilon;
    }
}
