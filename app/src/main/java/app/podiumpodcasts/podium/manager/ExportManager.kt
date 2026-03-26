package app.podiumpodcasts.podium.manager

import android.content.Context
import android.text.format.DateFormat
import app.podiumpodcasts.podium.api.db.AppDatabase
import app.podiumpodcasts.podium.api.opml.model.OpmlBody
import app.podiumpodcasts.podium.api.opml.model.OpmlFile
import app.podiumpodcasts.podium.api.opml.model.OpmlHead
import java.io.File
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

class ExportManager(
    val db: AppDatabase
) {
    suspend fun exportAsOpml(
        context: Context
    ): File {
        val podcasts = db.podcasts().allSync()

        val outlines = podcasts.map { podcast ->
            podcast.toOpmlOutline()
        }

        val dateCreated = ZonedDateTime.now().format(
            DateTimeFormatter.ofPattern("d MMM yy HH:mm:ss Z", Locale.US)
        )

        val opmlFile = OpmlFile(
            version = "2.0",
            head = OpmlHead(
                title = "podium Subscriptions",
                dateCreated = dateCreated
            ),
            body = OpmlBody(
                outlines = outlines
            )
        )

        val file = getExportFile(context, "opml")
        file.writeText(opmlFile.toString())
        return file
    }

    companion object {
        fun getExportDirectory(context: Context): File {
            return File(context.cacheDir, "export")
        }

        fun getExportFile(context: Context, extension: String): File {
            val exportDir = getExportDirectory(context)
            exportDir.deleteRecursively()
            exportDir.mkdirs()

            val timestamp = DateFormat.format("yyyy-MM-dd_HHmm", Date()).toString()
            return File(exportDir, "Export_$timestamp.$extension")
        }
    }
}