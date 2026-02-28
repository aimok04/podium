package app.podiumpodcasts.podium.utils

import okhttp3.Interceptor
import okhttp3.Response

class ContentSizeInterceptor(private val onSize: (Long) -> Unit) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        val responseBody = response.body

        val source = responseBody.source()
        source.request(Long.MAX_VALUE)
        val byteCount = source.buffer.size

        onSize(byteCount)

        return response
    }
}