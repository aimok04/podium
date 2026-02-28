package app.podiumpodcasts.podium.api.db.model

import androidx.room.Embedded
import androidx.room.Relation

data class PodcastSubscriptionBundle(
    @Embedded val subscription: PodcastSubscriptionModel,
    @Relation(
        parentColumn = "origin",
        entityColumn = "origin"
    )
    val podcast: PodcastModel
)