package app.podiumpodcasts.podium.api.gpodder

import app.podiumpodcasts.podium.BuildConfig
import app.podiumpodcasts.podium.api.gpodder.model.result.GpodderResult
import app.podiumpodcasts.podium.api.gpodder.route.Auth
import app.podiumpodcasts.podium.api.gpodder.route.Device
import app.podiumpodcasts.podium.api.gpodder.route.EpisodeActions
import app.podiumpodcasts.podium.api.gpodder.route.Subscriptions
import app.podiumpodcasts.podium.utils.json
import com.google.common.net.HttpHeaders
import io.ktor.client.HttpClient
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.AcceptAllCookiesStorage
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json

class GpodderClient(
    val deviceCaption: String,
    val deviceId: String,

    val baseUrl: String = "https://gpodder.net",
    val username: String,
    val password: String,
    val cookie: String
) {

    val httpClient = HttpClient {
        followRedirects = true

        install(DefaultRequest) {
            val appVersion = BuildConfig.VERSION_CODE

            val os = "Android ${android.os.Build.VERSION.RELEASE}"
            val model = android.os.Build.MODEL

            val github = "https://github.com/aimok04/podium"

            header(HttpHeaders.USER_AGENT, "podium/$appVersion ($os; $model; +$github)")
            header(HttpHeaders.COOKIE, cookie)
        }

        install(HttpCookies) {
            storage = AcceptAllCookiesStorage()
        }

        install(ContentNegotiation) {
            json(json = json)
        }
    }

    val auth = Auth(this)
    val device = Device(this)
    val episodeActions = EpisodeActions(this)
    val subscriptions = Subscriptions(this)

    suspend fun <T> parseResponse(
        response: HttpResponse,
        parseResult: suspend () -> T = { Unit as T }
    ): GpodderResult.Success<T> {
        if(response.status == HttpStatusCode.Unauthorized || response.status == HttpStatusCode.Forbidden)
            throw GpodderResult.Unauthenticated(response)

        if(response.status != HttpStatusCode.OK)
            throw GpodderResult.Failure(response)

        return GpodderResult.Success(
            result = parseResult()
        )
    }

}