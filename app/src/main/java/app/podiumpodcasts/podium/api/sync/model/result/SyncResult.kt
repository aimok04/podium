package app.podiumpodcasts.podium.api.sync.model.result

import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.request

interface SyncResult<T> {
    data class Success<T>(val result: T) : SyncResult<T>

    open class Failure(val response: HttpResponse) : Exception() {

        override fun toString(): String {
            return super.toString() + " / " + response.status.toString() + " / " + response.request.url
        }
    }

    class Unauthenticated(response: HttpResponse) : Failure(response)

    class NotSupported<T> : SyncResult<T>
}