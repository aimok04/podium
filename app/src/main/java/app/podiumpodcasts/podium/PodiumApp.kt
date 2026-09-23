package app.podiumpodcasts.podium

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import app.podiumpodcasts.podium.ui.DeepLink
import app.podiumpodcasts.podium.ui.Main
import app.podiumpodcasts.podium.ui.helper.LocalSettingsRepository
import app.podiumpodcasts.podium.ui.navigation.Home
import app.podiumpodcasts.podium.ui.theme.PodiumTheme
import app.podiumpodcasts.podium.ui.theme.ThemeMode

@Composable
fun PodiumApp(
    deepLink: DeepLink?
) {
    val settingsRepository = LocalSettingsRepository.current
    val themeMode by settingsRepository.appearance.themeMode.collectAsState(ThemeMode.SYSTEM)

    val darkTheme = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    PodiumTheme(darkTheme = darkTheme) {
        Box(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Main(deepLink)
        }
    }
}