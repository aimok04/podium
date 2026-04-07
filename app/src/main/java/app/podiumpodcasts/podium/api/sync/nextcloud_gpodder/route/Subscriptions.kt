package app.podiumpodcasts.podium.api.sync.nextcloud_gpodder.route

import android.util.Log
import app.podiumpodcasts.podium.api.sync.model.result.SubscriptionsGetChangesResult
import app.podiumpodcasts.podium.api.sync.model.result.SyncResult
import app.podiumpodcasts.podium.api.sync.model.result.UploadChangesResult
import app.podiumpodcasts.podium.api.sync.nextcloud_gpodder.NextcloudGpodderClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.path
import io.ktor.http.takeFrom

class Subscriptions(
    val client: NextcloudGpodderClient
) : ApiRoute(client) {

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
    ): SyncResult.Success<UploadChangesResult> {
        val response = client.httpClient.post {
            url {
                takeFrom(client.baseUrl)
                path("index.php", "apps", "gpoddersync", "subscription_change", "create")
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
            Log.d("Body", response.bodyAsText())
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
    ): SyncResult.Success<SubscriptionsGetChangesResult> {
        val response = client.httpClient.get {
            url {
                takeFrom(client.baseUrl)
                path("index.php", "apps", "gpoddersync", "subscriptions")
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