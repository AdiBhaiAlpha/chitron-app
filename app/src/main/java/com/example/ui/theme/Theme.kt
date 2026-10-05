package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R

// Warm Editorial Palette from css/style.css
val LightBg = Color(0xFFFAF8F5)
val LightBgSecondary = Color(0xFFF2EFE9)
val LightBgTertiary = Color(0xFFE8E4DD)
val LightTextPrimary = Color(0xFF1A1A1A)
val LightTextSecondary = Color(0xFF6B6560)
val LightTextTertiary = Color(0xFF5E5852)
val LightBorder = Color(0xFFE0DCD6)
val LightAccent = Color(0xFF8B5E3C)
val LightAccentHover = Color(0xFF724E32)
val LightAccentContainer = Color(0xFFF3EAE0)

val DarkBg = Color(0xFF1A1917)
val DarkBgSecondary = Color(0xFF232220)
val DarkBgTertiary = Color(0xFF2E2C29)
val DarkTextPrimary = Color(0xFFE8E4DE)
val DarkTextSecondary = Color(0xFF9A9590)
val DarkTextTertiary = Color(0xFFA8A29C)
val DarkBorder = Color(0xFF333130)
val DarkAccent = Color(0xFFC49A6C)
val DarkAccentHover = Color(0xFFD4AA7C)
val DarkAccentContainer = Color(0xFF382D22)

val ArchiveSerifFontFamily = FontFamily(
    Font(R.font.noto_serif_bengali_regular, FontWeight.Normal),
    Font(R.font.noto_serif_bengali_bold, FontWeight.Bold),
    Font(R.font.noto_serif_bengali_bold, FontWeight.SemiBold)
)

val ArchiveSansFontFamily = FontFamily(
    Font(R.font.kalpurush, FontWeight.Normal),
    Font(R.font.kalpurush, FontWeight.Medium),
    Font(R.font.noto_serif_bengali_bold, FontWeight.Bold)
)

private val LightColorScheme = lightColorScheme(
    primary = LightAccent,
    onPrimary = Color.White,
    primaryContainer = LightAccentContainer,
    onPrimaryContainer = LightAccentHover,
    secondary = LightTextSecondary,
    onSecondary = Color.White,
    secondaryContainer = LightBgSecondary,
    onSecondaryContainer = LightTextPrimary,
    tertiary = LightAccentHover,
    background = LightBg,
    onBackground = LightTextPrimary,
    surface = LightBg,
    onSurface = LightTextPrimary,
    surfaceVariant = LightBgSecondary,
    onSurfaceVariant = LightTextSecondary,
    outline = LightBorder,
    outlineVariant = LightBgTertiary
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkAccent,
    onPrimary = Color(0xFF1A1917),
    primaryContainer = DarkAccentContainer,
    onPrimaryContainer = DarkAccentHover,
    secondary = DarkTextSecondary,
    onSecondary = DarkBg,
    secondaryContainer = DarkBgSecondary,
    onSecondaryContainer = DarkTextPrimary,
    tertiary = DarkAccentHover,
    background = DarkBg,
    onBackground = DarkTextPrimary,
    surface = DarkBg,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkBgSecondary,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder,
    outlineVariant = DarkBgTertiary
)

val ArchiveTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = ArchiveSerifFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 34.sp,
        lineHeight = 42.sp,
        letterSpacing = (-0.5).sp
    ),
    displayMedium = TextStyle(
        fontFamily = ArchiveSerifFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.3).sp
    ),
    headlineLarge = TextStyle(
        fontFamily = ArchiveSerifFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = ArchiveSerifFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 28.sp
    ),
    titleLarge = TextStyle(
        fontFamily = ArchiveSerifFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 26.sp
    ),
    titleMedium = TextStyle(
        fontFamily = ArchiveSansFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    titleSmall = TextStyle(
        fontFamily = ArchiveSansFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = ArchiveSansFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 26.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = ArchiveSansFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 22.sp
    ),
    bodySmall = TextStyle(
        fontFamily = ArchiveSansFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp
    ),
    labelLarge = TextStyle(
        fontFamily = ArchiveSansFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    labelMedium = TextStyle(
        fontFamily = ArchiveSansFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelSmall = TextStyle(
        fontFamily = ArchiveSansFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 15.sp
    )
)

val ArchiveShapes = Shapes(
    small = RoundedCornerShape(6.dp),
    medium = RoundedCornerShape(10.dp),
    large = RoundedCornerShape(14.dp)
)

@Composable
fun ChitronsArchiveTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ArchiveTypography,
        shapes = ArchiveShapes,
        content = content
    )
}
