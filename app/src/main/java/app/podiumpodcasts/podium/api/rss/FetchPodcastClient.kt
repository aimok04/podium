package app.podiumpodcasts.podium.api.rss

import com.prof18.rssparser.RssParser
import com.prof18.rssparser.model.RssChannel
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.head
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsBytes
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode

interface FetchPodcastClientResult {
    data class Success(
        val rssChannel: RssChannel,
        val fileSize: Long,
        val lastModified: String,
        val eTag: String,
        val contentLength: String
    ) : FetchPodcastClientResult

    class Unchanged(
        val reason: String
    ) : FetchPodcastClientResult

    data class Failure(val e: Exception) : FetchPodcastClientResult
}

class FetchPodcastClient(
    val client: HttpClient = HttpClient { }
) {
    suspend fun fetch(
        origin: String,
        lastModified: String,
        eTag: String,
        contentLength: String
    ): FetchPodcastClientResult {
        try {
            val headRequest = client.head(origin) {
                header(HttpHeaders.AcceptEncoding, "identity")
                header(HttpHeaders.IfNoneMatch, eTag)
                header(HttpHeaders.IfModifiedSince, lastModified)
            }

            val newContentLength = headRequest.headers[HttpHeaders.ContentLength]
            if(contentLength == newContentLength)
                if(newContentLength.length > 2) return FetchPodcastClientResult.Unchanged("content-length equal, $newContentLength")

            when(headRequest.status) {
                HttpStatusCode.OK -> {
                    val response = client.get(origin) {
                        header(HttpHeaders.IfNoneMatch, eTag)
                        header(HttpHeaders.IfModifiedSince, lastModified)
                    }

                    val bytes = response.bodyAsBytes()
                    val string = bytes.decodeToString()

                    when(response.status) {
                        HttpStatusCode.OK -> {
                            return FetchPodcastClientResult.Success(
                                rssChannel = RssParser().parse(string),
                                fileSize = bytes.size.toLong(),
                                eTag = response.headers[HttpHeaders.ETag] ?: "",
                                lastModified = response.headers[HttpHeaders.LastModified] ?: "",
                                contentLength = newContentLength ?: ""
                            )
                        }

                        HttpStatusCode.NotModified -> {
                            return FetchPodcastClientResult.Unchanged("ETAG or LAST-MODIFIED get")
                        }
                    }
                }

                HttpStatusCode.NotModified -> {
                    return FetchPodcastClientResult.Unchanged("ETAG or LAST-MODIFIED head")
                }
            }

            throw Exception("UNHANDLED STATUS CODE ${headRequest.status}")
        } catch(e: Exception) {
            e.printStackTrace()
            return FetchPodcastClientResult.Failure(e)
        }
    }
}