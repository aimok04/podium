package app.podiumpodcasts.podium.ui.route.content

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.collectAsLazyPagingItems
import app.podiumpodcasts.podium.R
import app.podiumpodcasts.podium.ui.component.common.BackButton
import app.podiumpodcasts.podium.ui.component.media.FloatingMediaPlayerSpacer
import app.podiumpodcasts.podium.ui.component.model.PodcastCard
import app.podiumpodcasts.podium.ui.helper.LocalDatabase
import app.podiumpodcasts.podium.ui.vm.home.LocallyAvailableViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LocallyAvailableRoute(
    onClickPodcast: (origin: String) -> Unit,
    onBack: () -> Unit
) {
    val db = LocalDatabase.current
    val vm = viewModel { LocallyAvailableViewModel(db) }

    val locallyAvailable = vm.locallyAvailable.collectAsLazyPagingItems()

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    BackButton { onBack() }
                },
                title = {
                    Text(stringResource(R.string.route_locally_available))
                }
            )
        }
    ) { inset ->
        LazyVerticalGrid(
            state = when(locallyAvailable.itemCount) {
                0 -> LazyGridState()
                else -> vm.lazyGridState
            },
            columns = GridCells.Adaptive(100.dp),
            modifier = Modifier.padding(inset),
            contentPadding = PaddingValues(16.dp),
        ) {
            items(locallyAvailable.itemCount) {
                val item = locallyAvailable[it] ?: return@items

                PodcastCard(
                    podcast = item,
                    onClick = { onClickPodcast(item.origin) }
                )
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                FloatingMediaPlayerSpacer()
            }
        }
    }
}