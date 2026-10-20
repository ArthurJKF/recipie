package com.example.recipie.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val RecipieColors = lightColorScheme(
    primary = GrayDark, onPrimary = GraySurface,
    primaryContainer = GrayPanel, onPrimaryContainer = GrayText,
    secondary = GrayDark, onSecondary = GraySurface,
    secondaryContainer = Color(0xFFCCCCCC), onSecondaryContainer = GrayText,
    tertiary = GrayDark, onTertiary = GraySurface,
    tertiaryContainer = GrayPanel, onTertiaryContainer = GrayText,
    background = GrayBackground, onBackground = GrayText,
    surface = GraySurface, onSurface = GrayText,
    surfaceVariant = GrayPanel, onSurfaceVariant = Color(0xFF505050),
    surfaceTint = GrayDark,
    inverseSurface = GrayDark, inverseOnSurface = GraySurface,
    inversePrimary = Color(0xFFD0D0D0),
    error = GrayDark, onError = GraySurface,
    errorContainer = Color(0xFFD0D0D0), onErrorContainer = GrayText,
    outline = GrayOutline, outlineVariant = Color(0xFFAAAAAA),
    scrim = Color(0xFF000000),
    surfaceBright = GraySurface, surfaceDim = Color(0xFFCCCCCC),
    surfaceContainerLowest = GraySurface, surfaceContainerLow = Color(0xFFEEEEEE),
    surfaceContainer = Color(0xFFE8E8E8), surfaceContainerHigh = GrayPanel,
    surfaceContainerHighest = Color(0xFFD0D0D0)
)

private val PlainShapes = Shapes(
    extraSmall = RoundedCornerShape(0.dp),
    small = RoundedCornerShape(0.dp),
    medium = RoundedCornerShape(0.dp),
    large = RoundedCornerShape(0.dp),
    extraLarge = RoundedCornerShape(0.dp)
)

@Composable
fun ReciPieTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = RecipieColors,
        typography = Typography,
        shapes = PlainShapes,
        content = content
    )
}
