package app.podiumpodcasts.podium.api.apple.model.top

import app.podiumpodcasts.podium.api.model.PodcastPreviewModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TopPodcastsResponse(
    val feed: Feed,
)

@Serializable
data class Feed(
    val author: Author,
    val entry: List<Entry>,
    val updated: Updated,
    val rights: Rights,
    val title: Title,
    val icon: Icon,
    val link: List<Link>,
    val id: Id,
)

@Serializable
data class Author(
    val name: Name,
    val uri: Uri,
)

@Serializable
data class Name(
    val label: String,
)

@Serializable
data class Uri(
    val label: String,
)

@Serializable
data class Entry(
    @SerialName("im:name")
    val imName: ImName,
    @SerialName("im:image")
    val imImage: List<ImImage>,
    val summary: Summary,
    @SerialName("im:price")
    val imPrice: ImPrice,
    @SerialName("im:contentType")
    val imContentType: ImContentType,
    val rights: Rights? = null,
    val title: Title,
    val link: Link,
    val id: Id,
    @SerialName("im:artist")
    val imArtist: ImArtist,
    val category: Category,
    @SerialName("im:releaseDate")
    val imReleaseDate: ImReleaseDate,
) {
    fun toPodcastPreview(): PodcastPreviewModel? {
        if(id.attributes?.imId == null) return null

        return PodcastPreviewModel(
            fetchUrl = "itunes-lookup:${id.attributes.imId}",
            link = link.attributes.href ?: "",
            title = title.label,
            description = summary.label,
            author = imArtist.label,
            imageUrl = imImage.last().label,
            languageCode = "unknown"
        )
    }
}

@Serializable
data class ImName(
    val label: String,
)

@Serializable
data class ImImage(
    val label: String,
    val attributes: ImImageAttributes,
)

@Serializable
data class ImImageAttributes(
    val height: String,
)

@Serializable
data class Summary(
    val label: String,
)

@Serializable
data class ImPrice(
    val label: String,
    val attributes: ImPriceAttributes,
)

@Serializable
data class ImPriceAttributes(
    val amount: String,
    val currency: String,
)

@Serializable
data class ImContentType(
    val attributes: ImContentTypeAttributes,
)

@Serializable
data class ImContentTypeAttributes(
    val term: String,
    val label: String,
)

@Serializable
data class Rights(
    val label: String,
)

@Serializable
data class Title(
    val label: String,
)

@Serializable
data class Link(
    val attributes: LinkAttributes,
)

@Serializable
data class LinkAttributes(
    val rel: String? = null,
    val type: String? = null,
    val href: String? = null
)

@Serializable
data class Id(
    val label: String,
    val attributes: IdAttribute? = null
)

@Serializable
data class IdAttribute(
    @SerialName("im:id")
    val imId: String,
)

@Serializable
data class ImArtist(
    val label: String,
    val attributes: ImArtistAttributes? = null,
)

@Serializable
data class ImArtistAttributes(
    val href: String,
)

@Serializable
data class Category(
    val attributes: CategoryAttributes,
)

@Serializable
data class CategoryAttributes(
    @SerialName("im:id")
    val imId: String,
    val term: String,
    val scheme: String,
    val label: String,
)

@Serializable
data class ImReleaseDate(
    val label: String,
    val attributes: ImReleaseDateAttributes,
)

@Serializable
data class ImReleaseDateAttributes(
    val label: String,
)

@Serializable
data class Updated(
    val label: String,
)

@Serializable
data class Icon(
    val label: String,
)
