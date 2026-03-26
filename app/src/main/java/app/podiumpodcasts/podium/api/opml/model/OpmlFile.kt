package app.podiumpodcasts.podium.api.opml.model

import app.podiumpodcasts.podium.utils.xml
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName("opml")
data class OpmlFile(
    val version: String,
    @XmlElement(true) val head: OpmlHead,
    @XmlElement(true) val body: OpmlBody
) {
    companion object {
        fun parse(xml: String): OpmlFile {
            return app.podiumpodcasts.podium.utils.xml.decodeFromString<OpmlFile>(xml)
        }
    }

    override fun toString(): String {
        return xml.encodeToString(this)
    }
}

@Serializable
@XmlSerialName("head")
data class OpmlHead(
    @XmlElement(true) val title: String,
    @XmlElement(true) val dateCreated: String? = null
)

@Serializable
@XmlSerialName("body")
data class OpmlBody(
    @XmlSerialName("outline")
    var outlines: List<OpmlOutline>
)

@Serializable
@XmlSerialName("outline")
data class OpmlOutline(
    val text: String,
    val title: String? = null,
    val type: String? = null,
    val xmlUrl: String? = null,
    val htmlUrl: String? = null
)