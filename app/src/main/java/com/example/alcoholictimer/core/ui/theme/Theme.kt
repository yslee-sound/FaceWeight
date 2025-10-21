package com.example.alcoholictimer.core.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.activity.ComponentActivity
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.WindowInsetsCompat

private val DarkColorScheme = darkColorScheme(
    primary = BluePrimaryDark,
    onPrimary = Color.Black,
    primaryContainer = BluePrimaryDark,
    secondary = AmberSecondaryDark,
    onSecondary = Color.Black,
    tertiary = GreenTertiaryDark,
    onTertiary = Color.Black,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    error = ErrorDark,
    onError = Color.Black
)

private val LightColorScheme = lightColorScheme(
    primary = BluePrimaryLight,
    onPrimary = Color.White,
    primaryContainer = BluePrimaryContainer,
    secondary = AmberSecondaryLight,
    onSecondary = Color.White,
    tertiary = GreenTertiaryLight,
    onTertiary = Color.White,
    surface = SurfaceLight,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariant,
    error = ErrorLight,
    onError = Color.White
)

@Composable
fun AlcoholicTimerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    applySystemBars: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(colorScheme = colorScheme, typography = AppTypography, content = content)

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context as? Activity ?: return@SideEffect
            val window = activity.window

            // 상태바/내비바를 화면과 동일한 화이트로 지정해 경계선 시각 차이를 제거
            val statusBarColor = Color.White
            val navBarColor = Color.White

            if (applySystemBars) {
                WindowCompat.setDecorFitsSystemWindows(window, false)
                val statusBarStyle = SystemBarStyle.light(statusBarColor.toArgb(), statusBarColor.toArgb())
                val navigationBarStyle = SystemBarStyle.light(navBarColor.toArgb(), navBarColor.toArgb())
                (activity as? ComponentActivity)?.enableEdgeToEdge(
                    statusBarStyle = statusBarStyle,
                    navigationBarStyle = navigationBarStyle
                )
            } else {
                WindowCompat.setDecorFitsSystemWindows(window, true)
                window.statusBarColor = statusBarColor.toArgb()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    window.navigationBarColor = navBarColor.toArgb()
                }
            }

            val controller = WindowInsetsControllerCompat(window, view)
            controller.isAppearanceLightStatusBars = true
            controller.isAppearanceLightNavigationBars = true
            controller.show(WindowInsetsCompat.Type.statusBars())
            controller.show(WindowInsetsCompat.Type.navigationBars())
        }
    }
}
