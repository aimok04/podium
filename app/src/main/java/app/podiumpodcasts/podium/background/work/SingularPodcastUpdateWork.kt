package app.podiumpodcasts.podium.background.work

import android.content.Context
import app.podiumpodcasts.podium.api.db.AppDatabase
import app.podiumpodcasts.podium.api.db.model.PodcastModel
import app.podiumpodcasts.podium.api.rss.FetchPodcastClient
import app.podiumpodcasts.podium.api.rss.FetchPodcastClientResult
import app.podiumpodcasts.podium.manager.DownloadManager
import app.podiumpodcasts.podium.utils.rss.toPodcast
import app.podiumpodcasts.podium.utils.rss.toPodcastEpisode

class SingularPodcastUpdateWork(
    val context: Context,
    val db: AppDatabase
) {

    private val fetchPodcastClient = FetchPodcastClient()

    suspend fun doWork(
        oldPodcast: PodcastModel
    ) {
        val episodeIds = db.podcastEpisodes().getEpisodeIds(oldPodcast.origin)

        val response = fetchPodcastClient.fetchNoCache(oldPodcast.origin)

        if(response !is FetchPodcastClientResult.Success)
            throw Exception(response.toString())

        val podcast =
            response.rssChannel.toPodcast(oldPodcast.origin, response.fileSize, oldPodcast)
        db.podcasts().update(podcast)

        val newEpisodes = response.rssChannel.items
            .filter { !episodeIds.contains("${podcast.origin}:${it.guid}") }
            .map { it.toPodcastEpisode(podcast = podcast, new = true) }

        db.podcastEpisodes()
            .insertAllAndUpdateNewEpisodesCount(
                podcast.origin, *newEpisodes.toTypedArray()
            )
        newEpisodes.forEach {
            db.podcastEpisodePlayStates().initState(it.id)
        }

        val subscription = db.podcastSubscriptions().getSync(podcast.origin)
        if(subscription?.enableAutoDownload == true) newEpisodes.forEach {
            try {
                DownloadManager.downloadEpisode(context, db, it.id)
            } catch(e: Exception) {
                e.printStackTrace()
            }
        }

        db.podcastSubscriptions()
            .logUpdate(origin = podcast.origin)
    }

}