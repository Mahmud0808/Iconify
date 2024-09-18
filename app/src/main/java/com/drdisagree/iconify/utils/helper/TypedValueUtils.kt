package com.drdisagree.iconify.utils.helper

import android.util.TypedValue
import androidx.annotation.IntRange

object TypedValueUtils {

    fun createComplexDimension(
        @IntRange(from = -0x800000, to = 0x7FFFFF) value: Int,
        @ComplexDimensionUnit units: Int
    ): Int {
        require(!(units < TypedValue.COMPLEX_UNIT_PX || units > TypedValue.COMPLEX_UNIT_MM)) { "Must be a valid COMPLEX_UNIT_*: $units" }

        return intToComplex(value) or units
    }

    private fun intToComplex(value: Int): Int {
        require(!(value < -0x800000 || value >= 0x800000)) { "Magnitude of the value is too large: $value" }

        return createComplex(value, TypedValue.COMPLEX_RADIX_23p0)
    }

    private fun createComplex(
        @IntRange(from = -0x800000, to = 0x7FFFFF) mantissa: Int,
        radix: Int
    ): Int {
        require(!(mantissa < -0x800000 || mantissa >= 0x800000)) { "Magnitude of mantissa is too large: $mantissa" }
        require(!(radix < TypedValue.COMPLEX_RADIX_23p0 || radix > TypedValue.COMPLEX_RADIX_0p23)) { "Invalid radix: $radix" }

        return (mantissa and TypedValue.COMPLEX_MANTISSA_MASK shl TypedValue.COMPLEX_MANTISSA_SHIFT
                or (radix shl TypedValue.COMPLEX_RADIX_SHIFT))
    }

    annotation class ComplexDimensionUnit
}
