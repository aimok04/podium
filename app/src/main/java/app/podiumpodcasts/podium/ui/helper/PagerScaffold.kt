package app.podiumpodcasts.podium.ui.helper

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.paging.compose.LazyPagingItems
import app.podiumpodcasts.podium.api.db.model.PodcastEpisodeBundle

@Composable
fun PagerScaffold(
    vararg pager: LazyPagingItems<*>,
    isEmpty: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    val isEmpty = pager.all { it.loadState.isIdle && it.itemCount == 0 }

    AnimatedContent(
        modifier = Modifier.fillMaxSize(),
        targetState = isEmpty
    ) {
        when(it) {
            true -> isEmpty()
            false -> content()
        }
    }
}