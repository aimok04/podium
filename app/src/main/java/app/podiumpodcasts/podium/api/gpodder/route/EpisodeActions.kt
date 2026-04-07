package app.podiumpodcasts.podium.api.gpodder.route

import app.podiumpodcasts.podium.api.gpodder.GpodderClient
import app.podiumpodcasts.podium.api.gpodder.model.episodeactions.EpisodeAction
import app.podiumpodcasts.podium.api.gpodder.model.result.EpisodeActionsGetResult
import app.podiumpodcasts.podium.api.gpodder.model.result.GpodderResult
import app.podiumpodcasts.podium.api.gpodder.model.result.UploadChangesResult
import app.podiumpodcasts.podium.utils.json
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.path
import io.ktor.http.takeFrom
import kotlinx.serialization.json.encodeToJsonElement

class EpisodeActions(
    val client: GpodderClient
) : ApiRoute(client) {

    /**
     * Upload episode actions
     *
     * @param actions List of episode actions
     * @return whether update was successful
     */
    suspend fun upload(
        actions: List<EpisodeAction>
    ): GpodderResult.Success<UploadChangesResult> {
        val response = client.httpClient.post {
            url {
                takeFrom(client.baseUrl)
                path("api", "2", "episodes", "${client.username}.json")
            }

            contentType(ContentType.Application.Json)

            setBody(
                json.encodeToJsonElement(actions)
            )
        }

        return client.parseResponse(
            response = response
        ) {
            response.body<UploadChangesResult>()
        }
    }

    /**
     * Get episode actions
     *
     * @param since UNIX timestamp (seconds)
     * @param aggregated Only return latest actions
     */
    suspend fun get(
        since: Long,
        aggregated: Boolean = true
    ): GpodderResult.Success<EpisodeActionsGetResult> {
        val response = client.httpClient.get {
            url {
                takeFrom(client.baseUrl)
                path("api", "2", "episodes", "${client.username}.json")
            }

            parameter("since", since)
            parameter("aggregated", aggregated)
        }

        return client.parseResponse(
            response = response
        ) {
            response.body<EpisodeActionsGetResult>()
        }
    }

}