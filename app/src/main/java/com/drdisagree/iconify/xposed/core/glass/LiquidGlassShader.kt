package com.drdisagree.iconify.xposed.core.glass

import android.graphics.Color
import android.graphics.RuntimeShader

object LiquidGlassShader {

    private const val REFRACTION_RATIO = 0.18f
    private const val DISPERSION = 0.06f
    private const val SATURATION = 1.15f

    private const val SOURCE = """
        uniform shader image;
        uniform float2 origin;
        uniform float2 size;
        uniform float radius;
        uniform float refraction;
        uniform float edge;
        uniform float dispersion;
        uniform float rim;
        uniform float reflection;
        uniform float shadow;
        uniform float saturation;
        uniform float4 dim;

        float roundedBox(float2 p, float2 halfSize, float r) {
            float2 q = abs(p) - halfSize + r;
            return length(max(q, 0.0)) + min(max(q.x, q.y), 0.0) - r;
        }

        half4 main(float2 coord) {
            float2 halfSize = size * 0.5;
            float2 p = coord - origin - halfSize;
            float2 e = float2(1.0, 0.0);
            float d = roundedBox(p, halfSize, radius);
            float2 normal = float2(
                roundedBox(p + e.xy, halfSize, radius) - roundedBox(p - e.xy, halfSize, radius),
                roundedBox(p + e.yx, halfSize, radius) - roundedBox(p - e.yx, halfSize, radius)
            );
            normal = normal / max(length(normal), 0.0001);

            float band = clamp(1.0 + d / max(edge, 1.0), 0.0, 1.0);
            float bend = band * band * band;
            float2 offset = -normal * refraction * bend;

            half4 color;
            color.r = image.eval(coord + offset * (1.0 + dispersion)).r;
            color.g = image.eval(coord + offset).g;
            color.b = image.eval(coord + offset * (1.0 - dispersion)).b;
            color.a = 1.0;
            half luma = dot(color.rgb, half3(0.2126, 0.7152, 0.0722));
            color.rgb = clamp(mix(half3(luma), color.rgb, half(saturation)), 0.0, 1.0);
            color.rgb = mix(color.rgb, half3(dim.rgb), half(dim.a));

            float rimBand = smoothstep(-2.5, 0.0, d);
            float light = clamp(dot(normal, normalize(float2(-0.5, -0.85))), 0.0, 1.0);
            float shade = clamp(dot(normal, normalize(float2(0.5, 0.85))), 0.0, 1.0);
            color.rgb += half3(rim * rimBand * (0.18 + 0.5 * light));
            color.rgb += half3(reflection * bend * 0.12 * light);
            color.rgb -= half3(shadow * bend * 0.06 * shade);
            return color;
        }
    """

    fun create(): RuntimeShader = RuntimeShader(SOURCE)

    fun update(
        shader: RuntimeShader,
        originX: Float,
        originY: Float,
        width: Float,
        height: Float,
        radius: Float,
        tint: Int,
        style: LiquidGlassStyle
    ) {
        val base = minOf(width, height) * REFRACTION_RATIO
        shader.setFloatUniform("origin", originX, originY)
        shader.setFloatUniform("size", width, height)
        shader.setFloatUniform("radius", radius.coerceAtMost(minOf(width, height) / 2f))
        shader.setFloatUniform("refraction", base * style.refraction)
        shader.setFloatUniform("edge", base.coerceAtLeast(1f))
        shader.setFloatUniform("dispersion", DISPERSION)
        shader.setFloatUniform("rim", style.rim)
        shader.setFloatUniform("reflection", style.reflection)
        shader.setFloatUniform("shadow", style.shadow)
        shader.setFloatUniform("saturation", SATURATION)
        shader.setFloatUniform(
            "dim",
            Color.red(tint) / 255f,
            Color.green(tint) / 255f,
            Color.blue(tint) / 255f,
            Color.alpha(tint) / 255f
        )
    }
}
