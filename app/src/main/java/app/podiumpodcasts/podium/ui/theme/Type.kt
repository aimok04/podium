package app.podiumpodcasts.podium.ui.theme

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import app.podiumpodcasts.podium.R

@OptIn(ExperimentalTextApi::class)
fun googleSans() = FontFamily(
    Font(R.font.google_sans_flex)
)

@OptIn(ExperimentalTextApi::class)
fun googleSansCode() = FontFamily(
    Font(R.font.google_sans_code)
)

@OptIn(ExperimentalTextApi::class)
fun googleSansChunky() = FontFamily(
    Font(
        resId = R.font.google_sans_flex,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(500),
            FontVariation.grade(100),
            FontVariation.width(115f)
        )
    )
)

@OptIn(ExperimentalTextApi::class)
fun googleSansVeryChunky() = FontFamily(
    Font(
        resId = R.font.google_sans_flex,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(700),
            FontVariation.grade(100),
            FontVariation.width(180f)
        )
    )
)

@OptIn(ExperimentalTextApi::class)
fun googleSansDisplay() = FontFamily(
    Font(
        resId = R.font.google_sans_flex,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(700),
            FontVariation.grade(100),
            FontVariation.width(125f)
        )
    )
)

val defaultTypography = Typography()

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
val Typography = defaultTypography.copy(
    displayLarge = defaultTypography.displayLarge.copy(fontFamily = googleSans()),

    displayMedium = defaultTypography.displayMedium.copy(fontFamily = googleSans()),
    displayMediumEmphasized = defaultTypography.displayMediumEmphasized.copy(
        fontFamily = googleSansDisplay()
    ),

    displaySmall = defaultTypography.displaySmall.copy(fontFamily = googleSans()),
    displaySmallEmphasized = defaultTypography.displaySmallEmphasized.copy(
        fontFamily = googleSansDisplay()
    ),

    headlineLarge = defaultTypography.headlineLarge.copy(fontFamily = googleSans()),
    headlineLargeEmphasized = defaultTypography.headlineLargeEmphasized.copy(
        fontWeight = FontWeight.SemiBold,
        fontFamily = googleSansChunky()
    ),

    headlineMedium = defaultTypography.headlineMedium.copy(fontFamily = googleSans()),
    headlineMediumEmphasized = defaultTypography.headlineMediumEmphasized.copy(
        fontFamily = googleSansChunky()
    ),

    headlineSmall = defaultTypography.headlineSmall.copy(fontFamily = googleSans()),

    titleLarge = defaultTypography.titleLarge.copy(fontFamily = googleSans()),

    titleMedium = defaultTypography.titleMedium.copy(fontFamily = googleSans()),
    titleMediumEmphasized = defaultTypography.titleMediumEmphasized.copy(
        fontFamily = googleSansChunky()
    ),

    titleSmall = defaultTypography.titleSmall.copy(fontFamily = googleSans()),
    titleSmallEmphasized = defaultTypography.titleSmallEmphasized.copy(
        fontFamily = googleSansChunky()
    ),

    bodyLarge = defaultTypography.bodyLarge.copy(fontFamily = googleSans()),
    bodyMedium = defaultTypography.bodyMedium.copy(fontFamily = googleSans()),
    bodySmall = defaultTypography.bodySmall.copy(fontFamily = googleSans()),

    labelLarge = defaultTypography.labelLarge.copy(fontFamily = googleSans()),
    labelLargeEmphasized = defaultTypography.labelLargeEmphasized.copy(
        fontFamily = googleSansVeryChunky()
    ),

    labelMedium = defaultTypography.labelMedium.copy(fontFamily = googleSans()),
    labelMediumEmphasized = defaultTypography.labelMediumEmphasized.copy(
        fontFamily = googleSansCode()
    ),

    labelSmall = defaultTypography.labelSmall.copy(fontFamily = googleSans()),
    labelSmallEmphasized = defaultTypography.labelMediumEmphasized.copy(
        fontFamily = googleSansVeryChunky()
    ),
)