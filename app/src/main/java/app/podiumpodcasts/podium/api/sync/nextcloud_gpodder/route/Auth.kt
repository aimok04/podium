package app.podiumpodcasts.podium.api.sync.nextcloud_gpodder.route

import app.podiumpodcasts.podium.api.sync.model.result.SyncResult
import app.podiumpodcasts.podium.api.sync.nextcloud_gpodder.NextcloudGpodderClient
import app.podiumpodcasts.podium.api.sync.nextcloud_gpodder.model.Poll
import app.podiumpodcasts.podium.api.sync.nextcloud_gpodder.model.PollResult
import app.podiumpodcasts.podium.api.sync.nextcloud_gpodder.model.StartLoginResult
import io.ktor.client.call.body
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.http.path
import io.ktor.http.takeFrom

class Auth(
    val client: NextcloudGpodderClient
) : ApiRoute(client) {

    /**
     * Start login process
     *
     * @return StartLoginResult containing login link and poll data
     */
    suspend fun startLogin(): SyncResult.Success<StartLoginResult> {
        val response = client.httpClient.post {
            url {
                takeFrom(client.baseUrl)
                path("index.php", "login", "v2")
            }
        }

        return client.parseResponse(
            response = response
        ) {
            response.body<StartLoginResult>()
        }
    }

    /**
     * poll login state
     *
     * @return PollResult either containing PollResult.Successful with loginName and appPassword or PollResult.Unsuccessful
     */
    suspend fun poll(
        poll: Poll
    ): SyncResult.Success<PollResult> {
        val response = client.httpClient.post {
            url {
                takeFrom(poll.endpoint)
                parameter("token", poll.token)
            }
        }

        return client.parseResponse(
            response = response
        ) {
            try {
                response.body<PollResult.Successful>()
            } catch(e: Exception) {
                e.printStackTrace()
                PollResult.Unsuccessful
            }
        }
    }

}