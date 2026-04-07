package app.podiumpodcasts.podium.api.sync.nextcloud_gpodder

import android.os.Build
import app.podiumpodcasts.podium.BuildConfig
import app.podiumpodcasts.podium.api.sync.SyncClient
import app.podiumpodcasts.podium.api.sync.model.result.SyncResult
import app.podiumpodcasts.podium.api.sync.nextcloud_gpodder.route.Auth
import app.podiumpodcasts.podium.api.sync.nextcloud_gpodder.route.EpisodeActions
import app.podiumpodcasts.podium.api.sync.nextcloud_gpodder.route.Subscriptions
import app.podiumpodcasts.podium.utils.json
import com.google.common.net.HttpHeaders
import io.ktor.client.HttpClient
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.basicAuth
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json

class NextcloudGpodderClient(
    val baseUrl: String,
    val username: String = "",
    val password: String = ""
) : SyncClient {

    val httpClient = HttpClient {
        followRedirects = true

        install(DefaultRequest) {
            val appVersion = BuildConfig.VERSION_CODE

            val os = "Android ${Build.VERSION.RELEASE}"
            val model = Build.MODEL

            val github = "https://github.com/aimok04/podium"

            header(HttpHeaders.USER_AGENT, "podium/$appVersion ($os; $model; +$github)")
            basicAuth(username, password)
        }

        install(ContentNegotiation) {
            json(json = json)
        }
    }

    val auth = Auth(this)
    val episodeActions = EpisodeActions(this)
    val subscriptions = Subscriptions(this)

    suspend fun <T> parseResponse(
        response: HttpResponse,
        parseResult: suspend () -> T = { Unit as T }
    ): SyncResult.Success<T> {
        if(response.status == HttpStatusCode.Unauthorized || response.status == HttpStatusCode.Forbidden)
            throw SyncResult.Unauthenticated(response)

        if(response.status != HttpStatusCode.OK)
            throw SyncResult.Failure(response)

        return SyncResult.Success(
            result = parseResult()
        )
    }

}