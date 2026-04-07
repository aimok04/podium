package app.podiumpodcasts.podium.utils

import nl.adaptivity.xmlutil.ExperimentalXmlUtilApi
import nl.adaptivity.xmlutil.serialization.DefaultXmlSerializationPolicy
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.serialization.XmlConfig.Companion.IGNORING_UNKNOWN_CHILD_HANDLER

@OptIn(ExperimentalXmlUtilApi::class)
val xml = XML {
    autoPolymorphic = true
    repairNamespaces = true

    policy = DefaultXmlSerializationPolicy {
        unknownChildHandler = IGNORING_UNKNOWN_CHILD_HANDLER
    }
}