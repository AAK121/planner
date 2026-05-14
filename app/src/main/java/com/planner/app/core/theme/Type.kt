package com.planner.app.core.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.planner.app.R

private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs,
)

private val dmSansFont = GoogleFont("DM Sans")
private val newsreaderFont = GoogleFont("Newsreader")

val DmSans = FontFamily(
    Font(googleFont = dmSansFont, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = dmSansFont, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = dmSansFont, fontProvider = provider, weight = FontWeight.SemiBold),
)

val Newsreader = FontFamily(
    Font(
        googleFont = newsreaderFont,
        fontProvider = provider,
        weight = FontWeight.Normal,
        style = FontStyle.Italic,
    ),
)

val PlannerTypography = Typography(
    // 28sp — date title, screen headers
    displayMedium = TextStyle(
        fontFamily = DmSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
    ),
    // 24sp — section titles
    displaySmall = TextStyle(
        fontFamily = DmSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
    ),
    // 18sp — card headings
    headlineMedium = TextStyle(
        fontFamily = DmSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
    ),
    // 16sp — activity names, body emphasis
    titleMedium = TextStyle(
        fontFamily = DmSans,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp,
    ),
    // 15sp — welcome description
    titleSmall = TextStyle(
        fontFamily = DmSans,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 21.sp,
    ),
    // 14sp — body, form inputs
    bodyLarge = TextStyle(
        fontFamily = DmSans,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    // 13sp — meta info, timestamps
    bodyMedium = TextStyle(
        fontFamily = DmSans,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
    ),
    // 12sp — labels, pills
    bodySmall = TextStyle(
        fontFamily = DmSans,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
    // 11sp — section labels (uppercase)
    labelLarge = TextStyle(
        fontFamily = DmSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 1.5.sp,
    ),
    // 10sp — nav bar text
    labelMedium = TextStyle(
        fontFamily = DmSans,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 13.sp,
    ),
    // 9sp — chart labels
    labelSmall = TextStyle(
        fontFamily = DmSans,
        fontWeight = FontWeight.Normal,
        fontSize = 9.sp,
        lineHeight = 12.sp,
    ),
)

// Serif italic style for greetings — used inline where needed
val serifGreeting = TextStyle(
    fontFamily = Newsreader,
    fontWeight = FontWeight.Normal,
    fontStyle = FontStyle.Italic,
    fontSize = 28.sp,
    lineHeight = 34.sp,
)
