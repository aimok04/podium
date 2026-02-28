package app.podiumpodcasts.podium.api.apple

import app.podiumpodcasts.podium.api.apple.route.Lookup
import app.podiumpodcasts.podium.api.apple.route.Search
import app.podiumpodcasts.podium.api.apple.route.TopPodcasts
import app.podiumpodcasts.podium.utils.json
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json

class ApplePodcastClient {

    val httpClient = HttpClient {
        followRedirects = true

        install(ContentNegotiation) {
            json(json = json)
        }
    }

    val lookup = Lookup(this)
    val search = Search(this)
    val topPodcasts = TopPodcasts(this)

}