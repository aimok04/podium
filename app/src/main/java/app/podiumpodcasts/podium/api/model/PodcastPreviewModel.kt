package app.podiumpodcasts.podium.api.model

data class PodcastPreviewModel(
    var fetchUrl: String,
    val link: String,
    val title: String,
    val description: String,
    val author: String,
    val imageUrl: String,
    val languageCode: String
)