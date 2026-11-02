package com.drdisagree.iconify.xposed.core.resources

enum class ResourceHookType(
    val resourceType: String,
    val methods: List<String>,
    val replaceInTypedArray: Boolean = false,
    val supportsOffset: Boolean = false
) {
    BOOLEAN(
        resourceType = "bool",
        methods = listOf("getBoolean")
    ),
    INTEGER(
        resourceType = "integer",
        methods = listOf("getInteger")
    ),
    DIMENSION(
        resourceType = "dimen",
        methods = listOf("getDimension", "getDimensionPixelOffset", "getDimensionPixelSize"),
        supportsOffset = true
    ),
    DRAWABLE(
        resourceType = "drawable",
        methods = listOf("getDrawableForDensity"),
        replaceInTypedArray = true
    );

    fun convertsToPixels(method: String): Boolean =
        this == DIMENSION && method != "getDimension"
}
