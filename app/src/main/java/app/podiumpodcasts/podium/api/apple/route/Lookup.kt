package app.podiumpodcasts.podium.api.apple.route

import app.podiumpodcasts.podium.api.apple.ApplePodcastClient
import app.podiumpodcasts.podium.api.apple.model.top.LookupResponse
import app.podiumpodcasts.podium.utils.json
import io.ktor.client.call.body
import io.ktor.client.request.get

class Lookup(
    val client: ApplePodcastClient
) : ApiRoute(client) {

    suspend fun getRssFeedUrl(
        id: String
    ): String {
        val body = client.httpClient.get("https://itunes.apple.com/lookup?id=$id")
            .body<String>()

        val response = json.decodeFromString<LookupResponse>(body)
        return response.results.first().feedUrl
    }

}