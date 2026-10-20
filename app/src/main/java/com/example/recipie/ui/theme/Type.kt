package com.example.recipie.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private fun plainText(size: Int, lineHeight: Int, bold: Boolean = false) = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = 0.sp
)

val Typography = Typography(
    displayLarge = plainText(24, 30, true),
    displayMedium = plainText(22, 28, true),
    displaySmall = plainText(20, 26, true),
    headlineLarge = plainText(22, 28, true),
    headlineMedium = plainText(20, 26, true),
    headlineSmall = plainText(18, 24, true),
    titleLarge = plainText(17, 23, true),
    titleMedium = plainText(16, 22, true),
    titleSmall = plainText(14, 20, true),
    bodyLarge = plainText(16, 22),
    bodyMedium = plainText(14, 20),
    bodySmall = plainText(12, 18),
    labelLarge = plainText(14, 20, true),
    labelMedium = plainText(12, 18),
    labelSmall = plainText(11, 16)
)
