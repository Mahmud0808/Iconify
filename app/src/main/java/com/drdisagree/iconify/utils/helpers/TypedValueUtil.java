package com.drdisagree.iconify.utils.helpers;

import android.util.TypedValue;

import androidx.annotation.IntRange;

public class TypedValueUtil {

    public static int createComplexDimension(
            @IntRange(from = -0x800000, to = 0x7FFFFF) int value,
            @ComplexDimensionUnit int units) {
        if (units < TypedValue.COMPLEX_UNIT_PX || units > TypedValue.COMPLEX_UNIT_MM) {
            throw new IllegalArgumentException("Must be a valid COMPLEX_UNIT_*: " + units);
        }
        return intToComplex(value) | units;
    }

    public static int intToComplex(int value) {
        if (value < -0x800000 || value >= 0x800000) {
            throw new IllegalArgumentException("Magnitude of the value is too large: " + value);
        }
        return createComplex(value, TypedValue.COMPLEX_RADIX_23p0);
    }

    private static int createComplex(@IntRange(from = -0x800000, to = 0x7FFFFF) int mantissa,
                                     int radix) {
        if (mantissa < -0x800000 || mantissa >= 0x800000) {
            throw new IllegalArgumentException("Magnitude of mantissa is too large: " + mantissa);
        }
        if (radix < TypedValue.COMPLEX_RADIX_23p0 || radix > TypedValue.COMPLEX_RADIX_0p23) {
            throw new IllegalArgumentException("Invalid radix: " + radix);
        }
        return ((mantissa & TypedValue.COMPLEX_MANTISSA_MASK) << TypedValue.COMPLEX_MANTISSA_SHIFT)
                | (radix << TypedValue.COMPLEX_RADIX_SHIFT);
    }

    public @interface ComplexDimensionUnit {
    }
}
