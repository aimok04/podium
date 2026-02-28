package app.podiumpodcasts.podium.ui

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.navigation3.runtime.NavKey
import app.podiumpodcasts.podium.AppActivity
import app.podiumpodcasts.podium.ui.navigation.Home
import app.podiumpodcasts.podium.utils.json
import kotlinx.serialization.Serializable

@Serializable
sealed class DeepLink {
    abstract val route: NavKey

    open val detailPaneKey: DetailPaneKey? = null
    open val showMediaPlayerBottomSheet: Boolean = false

    @Serializable
    class OpenPodcast(
        val origin: String
    ) : DeepLink() {
        override val route: NavKey
            get() = Home

        override val detailPaneKey: DetailPaneKey
            get() = DetailPaneKey.PodcastKey(origin)
    }

    @Serializable
    class OpenPodcastEpisode(
        val origin: String,
        val episodeId: String
    ) : DeepLink() {
        override val route: NavKey
            get() = Home

        override val detailPaneKey: DetailPaneKey
            get() = DetailPaneKey.EpisodeKey(origin, episodeId)
    }

    @Serializable
    class OpenMediaPlayer() : DeepLink() {
        override val route: NavKey
            get() = Home

        override val showMediaPlayerBottomSheet: Boolean
            get() = true
    }
}

fun DeepLink.asIntent(
    context: Context
): Intent {
    return Intent(context, AppActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        putExtra("EXTRA_DEEP_LINK", json.encodeToString(this@asIntent))
    }
}

fun DeepLink.asPendingIntent(
    context: Context,
    requestCode: Int
): PendingIntent? {
    val intent = asIntent(context)

    return PendingIntent.getActivity(
        context,
        requestCode,
        intent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )
}

fun Intent.extractDeepLink(): DeepLink? {
    return getStringExtra("EXTRA_DEEP_LINK")?.let {
        json.decodeFromString<DeepLink>(it)
    }
}