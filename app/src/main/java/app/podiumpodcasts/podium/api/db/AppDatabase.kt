package app.podiumpodcasts.podium.api.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import app.podiumpodcasts.podium.api.db.dao.ListDao
import app.podiumpodcasts.podium.api.db.dao.ListItemDao
import app.podiumpodcasts.podium.api.db.dao.PodcastDao
import app.podiumpodcasts.podium.api.db.dao.PodcastEpisodeDao
import app.podiumpodcasts.podium.api.db.dao.PodcastEpisodeDownloadDao
import app.podiumpodcasts.podium.api.db.dao.PodcastEpisodePlayStateDao
import app.podiumpodcasts.podium.api.db.dao.PodcastHistoryDao
import app.podiumpodcasts.podium.api.db.dao.PodcastSubscriptionDao
import app.podiumpodcasts.podium.api.db.dao.statistics.UpdatePodcastRunDao
import app.podiumpodcasts.podium.api.db.model.ListItemModel
import app.podiumpodcasts.podium.api.db.model.ListModel
import app.podiumpodcasts.podium.api.db.model.PodcastEpisodeDownloadModel
import app.podiumpodcasts.podium.api.db.model.PodcastEpisodeModel
import app.podiumpodcasts.podium.api.db.model.PodcastEpisodePlayStateModel
import app.podiumpodcasts.podium.api.db.model.PodcastHistoryModel
import app.podiumpodcasts.podium.api.db.model.PodcastModel
import app.podiumpodcasts.podium.api.db.model.PodcastSubscriptionModel
import app.podiumpodcasts.podium.api.db.model.statistics.UpdatePodcastRunModel

@Database(
    entities = [
        PodcastModel::class,
        PodcastEpisodeModel::class,
        PodcastEpisodePlayStateModel::class,
        PodcastHistoryModel::class,
        PodcastSubscriptionModel::class,
        PodcastEpisodeDownloadModel::class,

        ListModel::class,
        ListItemModel::class,

        UpdatePodcastRunModel::class
    ], version = 13
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun podcasts(): PodcastDao
    abstract fun podcastEpisodes(): PodcastEpisodeDao
    abstract fun podcastEpisodeDownloads(): PodcastEpisodeDownloadDao
    abstract fun podcastEpisodePlayStates(): PodcastEpisodePlayStateDao
    abstract fun podcastHistory(): PodcastHistoryDao
    abstract fun podcastSubscriptions(): PodcastSubscriptionDao

    abstract fun lists(): ListDao
    abstract fun listItems(): ListItemDao

    abstract fun statisticsUpdatePodcastRun(): UpdatePodcastRunDao
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE podcastSubscription ADD COLUMN lastUpdate INTEGER NOT NULL")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `podcastEpisodeDownload` (
                `episodeId` TEXT NOT NULL, 
                `state` INTEGER NOT NULL, 
                `filename` TEXT, 
                `timestamp` INTEGER NOT NULL, 
                PRIMARY KEY(`episodeId`)
            )
            """.trimIndent()
        )
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE `podcastEpisodeDownload` ADD COLUMN `progress` REAL NOT NULL DEFAULT 0.0"
        )
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `podcastEpisodeDownload` ADD COLUMN `size` INTEGER NOT NULL DEFAULT 0")

        db.execSQL(
            """
            CREATE TABLE `podcastEpisodeDownload_new` (
                `episodeId` TEXT NOT NULL, 
                `state` INTEGER NOT NULL, 
                `filename` TEXT, 
                `progress` INTEGER NOT NULL, 
                `size` INTEGER NOT NULL, 
                `timestamp` INTEGER NOT NULL, 
                PRIMARY KEY(`episodeId`)
            )
        """.trimIndent()
        )

        db.execSQL(
            """
            INSERT INTO `podcastEpisodeDownload_new` (`episodeId`, `state`, `filename`, `progress`, `size`, `timestamp`)
            SELECT `episodeId`, `state`, `filename`, CAST(`progress` AS INTEGER), `size`, `timestamp` 
            FROM `podcastEpisodeDownload`
        """.trimIndent()
        )

        db.execSQL("DROP TABLE `podcastEpisodeDownload`")
        db.execSQL("ALTER TABLE `podcastEpisodeDownload_new` RENAME TO `podcastEpisodeDownload`")
    }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE `podcastSubscription` ADD COLUMN `enableAutoDownload` INTEGER NOT NULL DEFAULT 0"
        )
    }
}

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE podcast ADD COLUMN fileSize INTEGER NOT NULL DEFAULT 0"
        )
    }
}

val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE podcastSubscription ADD COLUMN cacheETag TEXT NOT NULL DEFAULT ''"
        )

        db.execSQL(
            "ALTER TABLE podcastSubscription ADD COLUMN cacheLastModified TEXT NOT NULL DEFAULT ''"
        )
    }
}

val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `statisticsUpdatePodcastRun` (
                `timestamp` INTEGER NOT NULL, 
                `dataUsage` INTEGER NOT NULL, 
                PRIMARY KEY(`timestamp`)
            )
            """.trimIndent()
        )
    }
}

val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE podcastSubscription ADD COLUMN cacheContentLength TEXT NOT NULL DEFAULT ''"
        )
    }
}

val MIGRATION_10_11 = object : Migration(10, 11) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `list` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                `name` TEXT NOT NULL, 
                `description` TEXT NOT NULL, 
                `itemCount` INTEGER NOT NULL DEFAULT 0, 
                `imageUrls` TEXT, 
                `createdAt` INTEGER NOT NULL, 
                `isSystemList` INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `listItem` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                `listId` INTEGER NOT NULL, 
                `contentId` TEXT NOT NULL, 
                `isPodcast` INTEGER NOT NULL, 
                `position` INTEGER NOT NULL
            )
        """.trimIndent()
        )
    }
}

val MIGRATION_11_12 = object : Migration(11, 12) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("DROP TABLE IF EXISTS `listItem`")

        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `listItem` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                `listId` INTEGER NOT NULL, 
                `contentId` TEXT NOT NULL, 
                `isPodcast` INTEGER NOT NULL, 
                `position` INTEGER NOT NULL, 
                FOREIGN KEY(`listId`) REFERENCES `list`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE 
            )
        """.trimIndent()
        )
    }
}

val MIGRATION_12_13 = object : Migration(12, 13) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE podcast ADD COLUMN overrideTitle TEXT NOT NULL DEFAULT ''")

        db.execSQL("ALTER TABLE podcast ADD COLUMN skipBeginning INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE podcast ADD COLUMN skipEnding INTEGER NOT NULL DEFAULT 0")
    }
}

val ALL_MIGRATIONS = arrayOf(
    MIGRATION_1_2,
    MIGRATION_2_3,
    MIGRATION_3_4,
    MIGRATION_4_5,
    MIGRATION_5_6,
    MIGRATION_6_7,
    MIGRATION_7_8,
    MIGRATION_8_9,
    MIGRATION_9_10,
    MIGRATION_10_11,
    MIGRATION_11_12,
    MIGRATION_12_13
)