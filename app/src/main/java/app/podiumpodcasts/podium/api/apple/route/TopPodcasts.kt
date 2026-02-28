package app.podiumpodcasts.podium.api.apple.route

import app.podiumpodcasts.podium.api.apple.ApplePodcastClient
import app.podiumpodcasts.podium.api.apple.model.Genre
import app.podiumpodcasts.podium.api.apple.model.top.TopPodcastsResponse
import app.podiumpodcasts.podium.api.model.PodcastPreviewModel
import app.podiumpodcasts.podium.utils.json
import io.ktor.client.call.body
import io.ktor.client.request.get

class TopPodcasts(
    val client: ApplePodcastClient
) : ApiRoute(client) {

    suspend fun load(
        countryCode: String,
        limit: Int = 50,
        genre: Genre? = null
    ): List<PodcastPreviewModel> {
        val genreStr = genre?.let { "genre=${it.id}/" } ?: ""

        val body =
            client.httpClient.get("https://itunes.apple.com/$countryCode/rss/toppodcasts/limit=$limit/${genreStr}explicit=true/json")
                .body<String>()

        val response = json.decodeFromString<TopPodcastsResponse>(body)
        return response.feed.entry.mapNotNull { it.toPodcastPreview() }
    }

}