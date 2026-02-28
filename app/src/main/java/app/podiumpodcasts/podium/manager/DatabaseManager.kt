package app.podiumpodcasts.podium.manager

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.format.DateFormat
import androidx.core.content.FileProvider
import androidx.room.Room
import app.podiumpodcasts.podium.api.db.ALL_MIGRATIONS
import app.podiumpodcasts.podium.api.db.AppDatabase
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.Date
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

const val DATABASE_NAME = "podiumDB"
const val DATABASE_EXTENSION = "podiumdb"

val DATABASE_BACKUP_FILES = arrayOf(
    "podiumDB",
    "podiumDB-shm",
    "podiumDB-wal"
)

class DatabaseManager {
    companion object {
        fun build(context: Context): AppDatabase {
            return Room
                .databaseBuilder(
                    context = context,
                    klass = AppDatabase::class.java,
                    name = DATABASE_NAME
                )
                .addMigrations(*ALL_MIGRATIONS)
                .enableMultiInstanceInvalidation()
                .build()
        }

        fun getDatabaseFile(context: Context): File? {
            return context.getDatabasePath(DATABASE_NAME)
        }

        fun getBackupFile(context: Context): File {
            val backupDir = File(context.cacheDir, "backup")
            backupDir.deleteRecursively()
            backupDir.mkdirs()

            val timestamp = DateFormat.format("yyyy-MM-dd_HHmm", Date()).toString()
            return File(backupDir, "Backup_$timestamp.$DATABASE_EXTENSION")
        }

        fun writeBackupFile(context: Context, file: File): File {
            val dbDir = getDatabaseFile(context)?.parentFile

            FileOutputStream(file).use { outputStream ->
                ZipOutputStream(BufferedOutputStream(outputStream)).use { zipOutputStream ->
                    DATABASE_BACKUP_FILES.forEach { fileName ->
                        val file = File(dbDir, fileName)
                        if(!file.exists()) return@forEach

                        zipOutputStream.putNextEntry(ZipEntry(file.name))
                        file.inputStream().use { it.copyTo(zipOutputStream) }
                        zipOutputStream.closeEntry()
                    }
                }
            }

            return file
        }

        fun shareBackupFile(
            context: Context,
            file: File,
            title: String
        ): Boolean {
            val contentUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/octet-stream"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(
                Intent.createChooser(
                    shareIntent,
                    title
                )
            )

            return true
        }

        fun isRestoreFileValid(context: Context, uri: Uri): Boolean {
            context.contentResolver.openInputStream(uri)?.use { input ->
                ZipInputStream(input).use { zipInputStream ->
                    var entry = zipInputStream.nextEntry
                    while(entry != null) {
                        if(entry.name == DATABASE_NAME) return true
                        entry = zipInputStream.nextEntry
                    }
                }
            }

            return false
        }

        fun restoreFromBackup(context: Context, uri: Uri) {
            val dbDir = getDatabaseFile(context)!!.parent

            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                ZipInputStream(inputStream).use { zipInputStream ->
                    var entry = zipInputStream.nextEntry
                    while(entry != null) {
                        if(entry.name in DATABASE_BACKUP_FILES) {
                            val destination = File(dbDir, entry.name)
                            if(destination.exists()) destination.delete()

                            destination.outputStream().use { outputStream ->
                                zipInputStream.copyTo(outputStream)
                            }

                            zipInputStream.closeEntry()
                            entry = zipInputStream.nextEntry
                        }
                    }
                }
            }
        }
    }
}