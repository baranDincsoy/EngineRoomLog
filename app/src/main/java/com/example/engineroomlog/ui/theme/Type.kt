package com.example.engineroomlog.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.engineroomlog.R

// IBM Plex Sans — designed for technical/engineering contexts
val PlexSans = FontFamily(
    Font(R.font.ibm_plex_sans_regular, FontWeight.Normal),
    Font(R.font.ibm_plex_sans_medium, FontWeight.Medium),
    Font(R.font.ibm_plex_sans_semibold, FontWeight.SemiBold),
    Font(R.font.ibm_plex_sans_bold, FontWeight.Bold)
)

val Typography = Typography(
    headlineSmall = TextStyle(
        fontFamily = PlexSans, fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp, lineHeight = 32.sp
    ),
    titleLarge = TextStyle(
        fontFamily = PlexSans, fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp, lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontFamily = PlexSans, fontWeight = FontWeight.Medium,
        fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.15.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = PlexSans, fontWeight = FontWeight.Normal,
        fontSize = 16.sp, lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = PlexSans, fontWeight = FontWeight.Normal,
        fontSize = 14.sp, lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontFamily = PlexSans, fontWeight = FontWeight.Normal,
        fontSize = 12.sp, lineHeight = 16.sp
    ),
    labelLarge = TextStyle(
        fontFamily = PlexSans, fontWeight = FontWeight.Medium,
        fontSize = 14.sp, letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = PlexSans, fontWeight = FontWeight.Medium,
        fontSize = 12.sp, letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = PlexSans, fontWeight = FontWeight.Medium,
        fontSize = 11.sp, letterSpacing = 0.5.sp
    )
)