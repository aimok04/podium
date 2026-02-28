package app.podiumpodcasts.podium.utils.rss

import app.podiumpodcasts.podium.api.db.model.PodcastEpisodeModel
import app.podiumpodcasts.podium.api.db.model.PodcastModel
import app.podiumpodcasts.podium.ui.parseItunesDuration
import app.podiumpodcasts.podium.ui.parsePubDate
import app.podiumpodcasts.podium.utils.ContentSizeInterceptor
import com.prof18.rssparser.RssParser
import com.prof18.rssparser.RssParserBuilder
import com.prof18.rssparser.model.RssChannel
import com.prof18.rssparser.model.RssItem
import okhttp3.OkHttpClient

fun buildPodiumRssParser(
    onSize: (size: Long) -> Unit
): RssParser {
    return RssParserBuilder(
        callFactory = OkHttpClient.Builder()
            .addInterceptor(ContentSizeInterceptor(onSize))
            .build()
    ).build()
}

fun RssChannel.toPodcast(
    origin: String,
    fileSize: Long,
    oldPodcast: PodcastModel?
): PodcastModel {
    return PodcastModel(
        origin = origin,
        link = link ?: "",
        title = title ?: "",
        description = description ?: "",
        author = itunesChannelData?.author ?: "",
        imageUrl = image?.url ?: "",
        imageSeedColor = oldPodcast?.imageSeedColor ?: 0,
        languageCode = "unknown",
        fileSize = fileSize,

        overrideTitle = oldPodcast?.overrideTitle ?: "",
        skipBeginning = oldPodcast?.skipBeginning ?: 0,
        skipEnding = oldPodcast?.skipEnding ?: 0
    )
}

fun RssItem.toPodcastEpisode(
    podcast: PodcastModel,
    new: Boolean = false
): PodcastEpisodeModel {
    return PodcastEpisodeModel(
        id = "${podcast.origin}:$guid",
        guid = guid ?: "",
        origin = podcast.origin,
        link = link ?: "",
        title = title ?: "",
        description = description ?: "",
        imageUrl = (itunesItemData?.image ?: image ?: "").ifBlank { podcast.imageUrl },
        author = author ?: "",
        pubDate = parsePubDate(pubDate ?: ""),
        duration = itunesItemData?.duration?.let {
            parseItunesDuration(it)
        } ?: -1,
        audioUrl = audio ?: "",
        podcastTitle = podcast.title,
        imageSeedColor = podcast.imageSeedColor,
        new = new
    )
}