package app.podiumpodcasts.podium.api.apple.model.top

import app.podiumpodcasts.podium.api.model.PodcastPreviewModel
import kotlinx.serialization.Serializable

@Serializable
data class SearchResponse(
    val resultCount: Long,
    val results: List<SearchResult>,
)

@Serializable
data class SearchResult(
    val wrapperType: String,
    val kind: String,
    val artistId: Long? = null,
    val collectionId: Long,
    val trackId: Long,
    val artistName: String,
    val collectionName: String,
    val trackName: String,
    val collectionCensoredName: String,
    val trackCensoredName: String,
    val artistViewUrl: String? = null,
    val collectionViewUrl: String,
    val feedUrl: String? = null,
    val trackViewUrl: String,
    val artworkUrl30: String,
    val artworkUrl60: String,
    val artworkUrl100: String,
    val collectionPrice: Double,
    val trackPrice: Double,
    val collectionHdPrice: Long,
    val releaseDate: String? = null,
    val collectionExplicitness: String,
    val trackExplicitness: String,
    val trackCount: Long,
    val trackTimeMillis: Long? = null,
    val country: String,
    val currency: String,
    val primaryGenreName: String,
    val artworkUrl600: String,
    val genreIds: List<String>,
    val genres: List<String>,
    val contentAdvisoryRating: String? = null,
) {
    fun toPodcastPreview(): PodcastPreviewModel? {
        if(feedUrl == null) return null

        return PodcastPreviewModel(
            fetchUrl = feedUrl,
            link = trackViewUrl,
            title = trackName,
            description = releaseDate ?: "",
            author = artistName,
            imageUrl = artworkUrl600,
            languageCode = country.lowercase()
        )
    }
}