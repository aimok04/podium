package app.podiumpodcasts.podium.ui.modifier

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import coil3.compose.AsyncImagePainter.State
import com.valentinilk.shimmer.shimmer

class AsyncImageLoadingShimmerState() {
    var loadingState by mutableStateOf<State>(State.Loading(painter = null))
    fun onState(): (State) -> Unit {
        return { loadingState = it }
    }
}

@Composable
fun Modifier.asyncImageLoadingShimmer(
    state: AsyncImageLoadingShimmerState
): Modifier {
    return if(state.loadingState is State.Loading)
        shimmer()
    else
        this
}