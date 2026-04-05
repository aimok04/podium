package app.podiumpodcasts.podium.manager

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import app.podiumpodcasts.podium.api.db.AppDatabase
import app.podiumpodcasts.podium.api.db.model.PodcastEpisodeModel
import app.podiumpodcasts.podium.api.db.model.PodcastModel
import app.podiumpodcasts.podium.api.rss.FetchPodcastClient
import app.podiumpodcasts.podium.api.rss.FetchPodcastClientResult
import app.podiumpodcasts.podium.utils.rss.toPodcast
import app.podiumpodcasts.podium.utils.rss.toPodcastEpisode

interface AddPodcastResult {
    data class Duplicate(val duplicate: PodcastModel) : AddPodcastResult
    data class Created(val podcast: PodcastModel) : AddPodcastResult
}

class PodcastManager(
    val db: AppDatabase
) {

    private val fetchPodcastClient = FetchPodcastClient()

    suspend fun addPodcast(
        origin: String,
        seedColor: Color?
    ): AddPodcastResult {
        db.podcasts().getSync(origin)?.let { duplicate ->
            return AddPodcastResult.Duplicate(
                duplicate = duplicate
            )
        }

        val response = fetchPodcastClient.fetchNoCache(origin)

        if(response !is FetchPodcastClientResult.Success)
            throw Exception(response.toString())

        val podcast = response.rssChannel.toPodcast(origin, response.fileSize, null)
        val episodes = response.rssChannel.items.map { it.toPodcastEpisode(podcast = podcast) }

        return addPodcast(podcast, episodes, seedColor, false)
    }

    suspend fun addPodcast(
        podcast: PodcastModel,
        episodes: List<PodcastEpisodeModel>,
        seedColor: Color?,
        duplicateCheck: Boolean = true
    ): AddPodcastResult {
        if(duplicateCheck) db.podcasts().getSync(podcast.origin)?.let { duplicate ->
            return AddPodcastResult.Duplicate(
                duplicate = duplicate
            )
        }

        podcast.imageSeedColor = seedColor?.toArgb() ?: 0
        episodes.forEach { it.imageSeedColor = podcast.imageSeedColor }

        db.podcasts().insertAll(podcast)
        db.podcastEpisodes()._insertAll(*episodes.toTypedArray())
        episodes.forEach { db.podcastEpisodePlayStates().initState(it.id) }

        return AddPodcastResult.Created(
            podcast = podcast
        )
    }

}