package app.podiumpodcasts.podium.api.gpodder.model.result

import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.request

interface GpodderResult<T> {
    data class Success<T>(val result: T) : GpodderResult<T>

    open class Failure(val response: HttpResponse) : Exception() {

        override fun toString(): String {
            return super.toString() + " / " + response.status.toString() + " / " + response.request.url
        }
    }

    class Unauthenticated(response: HttpResponse) : Failure(response)
}