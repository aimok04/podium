package app.podiumpodcasts.podium.api.gpodder.route

import app.podiumpodcasts.podium.api.gpodder.GpodderClient
import app.podiumpodcasts.podium.api.gpodder.model.result.GpodderResult
import app.podiumpodcasts.podium.api.gpodder.model.result.SubscriptionsGetChangesResult
import app.podiumpodcasts.podium.api.gpodder.model.result.UploadChangesResult
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.path
import io.ktor.http.takeFrom

class Subscriptions(
    val client: GpodderClient
) : ApiRoute(client) {

    /**
     * Upload subscriptions
     *
     * @param origins List of subscription URLs
     * @return whether update was successful
     */
    suspend fun upload(
        origins: List<String>
    ): GpodderResult.Success<Any> {
        val response = client.httpClient.put {
            url {
                takeFrom(client.baseUrl)
                path("subscriptions", client.username, "${client.deviceId}.json")
            }

            contentType(ContentType.Application.Json)

            setBody(
                origins
            )
        }

        return client.parseResponse(
            response = response
        )
    }

    /**
     * Upload subscription changes
     *
     * @param add List of subscription URLs to add
     * @param remove List of subscription URLs to remove
     * @return whether update was successful
     */
    suspend fun uploadChanges(
        add: List<String>,
        remove: List<String>
    ): GpodderResult.Success<UploadChangesResult> {
        val response = client.httpClient.post {
            url {
                takeFrom(client.baseUrl)
                path("api", "2", "subscriptions", client.username, "${client.deviceId}.json")
            }

            contentType(ContentType.Application.Json)

            setBody(
                mapOf(
                    "add" to add,
                    "remove" to remove
                )
            )
        }

        return client.parseResponse(
            response = response
        ) {
            response.body<UploadChangesResult>()
        }
    }

    /**
     * Get subscription changes
     *
     * @param since UNIX timestamp (seconds)
     */
    suspend fun getChanges(
        since: Long
    ): GpodderResult.Success<SubscriptionsGetChangesResult> {
        val response = client.httpClient.get {
            url {
                takeFrom(client.baseUrl)
                path("api", "2", "subscriptions", client.username, "${client.deviceId}.json")
            }

            parameter("since", since)
        }

        return client.parseResponse(
            response = response
        ) {
            response.body<SubscriptionsGetChangesResult>()
        }
    }

}