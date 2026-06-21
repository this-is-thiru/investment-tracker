package com.thiru.wealthlens.shared.util.math;

public class DoubleUtil {
    private static final double EPSILON = 1e-9;

    public static boolean equal(double a, double b) {
        return Math.abs(a - b) < EPSILON;
    }
}
