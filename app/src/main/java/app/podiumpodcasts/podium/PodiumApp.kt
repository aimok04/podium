package app.podiumpodcasts.podium

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.podiumpodcasts.podium.ui.DeepLink
import app.podiumpodcasts.podium.ui.Main
import app.podiumpodcasts.podium.ui.navigation.Home
import app.podiumpodcasts.podium.ui.theme.PodiumTheme

@Composable
fun PodiumApp(
    deepLink: DeepLink?
) {
    PodiumTheme {
        Box(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Main(deepLink)
        }
    }
}