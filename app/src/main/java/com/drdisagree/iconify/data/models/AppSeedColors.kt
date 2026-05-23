package com.drdisagree.iconify.data.models

data class SeedColor(
    val name: String,
    val primaryColor: Long,
)

sealed class AppSeedColors(val seedColor: SeedColor) {

    data object Blue : AppSeedColors(
        SeedColor(
            name = "Blue",
            primaryColor = 0xFF2196F3
        )
    )

    data object Red : AppSeedColors(
        SeedColor(
            name = "Red",
            primaryColor = 0xFFF44336
        )
    )

    data object Green : AppSeedColors(
        SeedColor(
            name = "Green",
            primaryColor = 0xFF4CAF50
        )
    )

    data object Purple : AppSeedColors(
        SeedColor(
            name = "Purple",
            primaryColor = 0xFF9C27B0
        )
    )

    data object Orange : AppSeedColors(
        SeedColor(
            name = "Orange",
            primaryColor = 0xFFFF9800
        )
    )

    data object Teal : AppSeedColors(
        SeedColor(
            name = "Teal",
            primaryColor = 0xFF009688
        )
    )

    data object Pink : AppSeedColors(
        SeedColor(
            name = "Pink",
            primaryColor = 0xFFE91E63
        )
    )

    data object Brown : AppSeedColors(
        SeedColor(
            name = "Brown",
            primaryColor = 0xFF80471C
        )
    )
}

val allSeedColors = listOf(
    AppSeedColors.Blue,
    AppSeedColors.Red,
    AppSeedColors.Green,
    AppSeedColors.Purple,
    AppSeedColors.Orange,
    AppSeedColors.Teal,
    AppSeedColors.Pink,
    AppSeedColors.Brown
)