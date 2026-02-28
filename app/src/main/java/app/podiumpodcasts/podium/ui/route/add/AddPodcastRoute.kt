package app.podiumpodcasts.podium.ui.route.add

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.podiumpodcasts.podium.R
import app.podiumpodcasts.podium.ui.component.common.BackButton
import app.podiumpodcasts.podium.ui.view.model.AddPodcastView

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AddPodcastRoute(
    onOpenPodcast: (origin: String) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    BackButton {
                        onBack()
                    }
                },
                title = {
                    Text(stringResource(R.string.route_add_podcast))
                }
            )
        }
    ) { inset ->
        Box(
            Modifier.padding(inset)
        ) {
            AddPodcastView {
                onOpenPodcast(it)
                onBack()
            }
        }
    }
}