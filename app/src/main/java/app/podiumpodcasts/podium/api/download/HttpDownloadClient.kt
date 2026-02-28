package app.podiumpodcasts.podium.api.download

import io.ktor.client.HttpClient
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.contentLength
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readAvailable
import java.io.File
import java.nio.ByteBuffer

interface HttpDownloadClientResult {
    class Success : HttpDownloadClientResult
    data class Failure(val e: Exception) : HttpDownloadClientResult
}

class HttpDownloadClient(
    val client: HttpClient = HttpClient { }
) {
    suspend fun download(
        url: String,
        output: File,
        onProgress: (progress: Long, total: Long) -> Unit
    ): HttpDownloadClientResult {
        try {
            client.prepareGet(url).execute { httpResponse ->
                val channel: ByteReadChannel = httpResponse.bodyAsChannel()
                val totalBytes = httpResponse.contentLength() ?: -1L
                val fileChannel = output.outputStream().channel

                val buffer = ByteBuffer.allocate(8192)
                var progress = 0L

                while(!channel.isClosedForRead) {
                    buffer.clear()
                    val read = channel.readAvailable(buffer)
                    if(read == -1) break

                    buffer.flip()
                    while(buffer.hasRemaining())
                        fileChannel.write(buffer)

                    progress += read
                    if(totalBytes > 0) onProgress(progress, totalBytes)
                }
                fileChannel.close()
            }

            return HttpDownloadClientResult.Success()
        } catch(e: Exception) {
            e.printStackTrace()
            return HttpDownloadClientResult.Failure(e)
        } finally {
            client.close()
        }
    }
}