package com.countertap.business.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary             = AccentBlue,
    onPrimary           = TextPrimary,
    primaryContainer    = AccentBlueDark,
    onPrimaryContainer  = TextPrimary,
    secondary           = AccentBlueLight,
    onSecondary         = BackgroundDark,
    background          = BackgroundDark,
    onBackground        = TextPrimary,
    surface             = SurfaceColor,
    onSurface           = TextPrimary,
    surfaceVariant      = SurfaceVariant,
    onSurfaceVariant    = TextSecondary,
    outline             = DividerColor,
    error               = ErrorRed,
    onError             = TextPrimary
)

@Composable
fun CounterTapTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography  = CounterTapTypography,
        content     = content
    )
}
