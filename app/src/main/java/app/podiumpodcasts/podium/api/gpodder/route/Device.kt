package app.podiumpodcasts.podium.api.gpodder.route

import app.podiumpodcasts.podium.api.gpodder.GpodderClient
import app.podiumpodcasts.podium.api.gpodder.model.result.GpodderResult
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.path
import io.ktor.http.takeFrom

class Device(
    val client: GpodderClient
) : ApiRoute(client) {

    /**
     * Update device
     *
     * @param deviceType Type of device. Can be one of: desktop, laptop, mobile, server, other
     * @return whether update was successful
     */
    suspend fun update(
        deviceType: String = "mobile"
    ): GpodderResult.Success<Any> {
        val response = client.httpClient.post {
            url {
                takeFrom(client.baseUrl)
                path("api", "2", "devices", client.username, "${client.deviceId}.json")
            }

            contentType(ContentType.Application.Json)

            setBody(
                mapOf(
                    "caption" to client.deviceCaption,
                    "type" to deviceType
                )
            )
        }

        return client.parseResponse(
            response = response
        )
    }

}