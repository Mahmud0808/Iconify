package com.drdisagree.iconify.core.ui.components.others

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.drdisagree.iconify.core.common.LocalDarkMode

@Composable
fun coloredLogText(
    text: String
): AnnotatedString {
    val isDarkTheme = LocalDarkMode.current

    val infoColor = if (isDarkTheme) Color(0xFF66BB6A) else Color(0xFF81C784)
    val warnColor = if (isDarkTheme) Color(0xFFFFA726) else Color(0xFFFFB74D)
    val errorColor = MaterialTheme.colorScheme.error

    return buildAnnotatedString {
        when {
            text.startsWith("I: ") -> {
                withStyle(SpanStyle(color = infoColor)) {
                    append("I: ")
                }
                append(text.removePrefix("I: "))
            }

            text.startsWith("W: ") -> {
                withStyle(SpanStyle(color = warnColor)) {
                    append("W: ")
                }
                append(text.removePrefix("W: "))
            }

            text.startsWith("E: ") -> {
                withStyle(SpanStyle(color = errorColor)) {
                    append("E: ")
                }
                append(text.removePrefix("E: "))
            }

            else -> append(text)
        }
    }
}