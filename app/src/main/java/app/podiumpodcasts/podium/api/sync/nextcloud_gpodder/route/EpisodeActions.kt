package app.podiumpodcasts.podium.api.sync.nextcloud_gpodder.route

import app.podiumpodcasts.podium.api.sync.model.episodeactions.EpisodeAction
import app.podiumpodcasts.podium.api.sync.model.result.EpisodeActionsGetResult
import app.podiumpodcasts.podium.api.sync.model.result.SyncResult
import app.podiumpodcasts.podium.api.sync.model.result.UploadChangesResult
import app.podiumpodcasts.podium.api.sync.nextcloud_gpodder.NextcloudGpodderClient
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
    val client: NextcloudGpodderClient
) : ApiRoute(client) {

    /**
     * Upload episode actions
     *
     * @param actions List of episode actions
     * @return whether update was successful
     */
    suspend fun upload(
        actions: List<EpisodeAction>
    ): SyncResult.Success<UploadChangesResult> {
        val response = client.httpClient.post {
            url {
                takeFrom(client.baseUrl)
                path("index.php", "apps", "gpoddersync", "episode_action", "create")
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
     */
    suspend fun get(
        since: Long
    ): SyncResult.Success<EpisodeActionsGetResult> {
        val response = client.httpClient.get {
            url {
                takeFrom(client.baseUrl)
                path("index.php", "apps", "gpoddersync", "episode_action")
            }

            parameter("since", since)
        }

        return client.parseResponse(
            response = response
        ) {
            response.body<EpisodeActionsGetResult>()
        }
    }

}