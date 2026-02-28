package app.podiumpodcasts.podium.ui.vm

import android.content.Context
import android.text.format.Formatter
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.podiumpodcasts.podium.R
import app.podiumpodcasts.podium.SettingsRepository
import app.podiumpodcasts.podium.api.db.AppDatabase
import app.podiumpodcasts.podium.background.worker.PeriodicPodcastUpdateWorker
import app.podiumpodcasts.podium.manager.DatabaseManager
import app.podiumpodcasts.podium.manager.DownloadManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

val UPDATE_PODCASTS_INTERVAL_VALUES = listOf(
    15,
    30,
    45,
    60,
    120,
    180,
    240,
    300,
    360,
    420,
    480,
    540,
    600,
    660,
    720
)

enum class DeleteDownloadsAfterValues(
    val label: Int,
    val seconds: Int
) {
    DAY(R.string.route_settings_downloads_and_storage_delete_downloads_after_value_day, 86400),
    THREE_DAYS(
        R.string.route_settings_downloads_and_storage_delete_downloads_after_value_three_days,
        86400 * 3
    ),
    WEEK(
        R.string.route_settings_downloads_and_storage_delete_downloads_after_value_week,
        86400 * 7
    ),
    MONTH(
        R.string.route_settings_downloads_and_storage_delete_downloads_after_value_month,
        86400 * 30
    ),
    NEVER(R.string.route_settings_downloads_and_storage_delete_downloads_after_value_never, -1)
}

interface ExportDatabaseState {
    object Idle : ExportDatabaseState
    data class Writing(val fileName: String) : ExportDatabaseState
}

interface RoamingWarningDialogState {
    object Hide : RoamingWarningDialogState
    object ShowUpdate : RoamingWarningDialogState
    object ShowDownload : RoamingWarningDialogState
}

class SettingsViewModel(
    val db: AppDatabase,
    val repository: SettingsRepository
) : ViewModel() {

    val avgUpdateRunDataUsage = mutableStateOf<Long?>(null)

    val exportDatabaseState = mutableStateOf<ExportDatabaseState>(ExportDatabaseState.Idle)

    val updatePodcastsIntervalMinutesSliderState = mutableFloatStateOf(1f)
    val updatePodcastsIntervalMinutesTranslatedSliderState = mutableIntStateOf(60)

    val deleteDownloadsAfterSliderState = mutableFloatStateOf(0f)
    val deleteDownloadsAfterTranslatedSliderState = mutableStateOf(DeleteDownloadsAfterValues.DAY)

    val roamingWarningDialogState =
        mutableStateOf<RoamingWarningDialogState>(RoamingWarningDialogState.Hide)

    init {
        viewModelScope.launch {
            avgUpdateRunDataUsage.value = db.statisticsUpdatePodcastRun()
                .getAvgDataUsage()
        }
    }

    fun calculateDatabaseSize(context: Context): String? {
        val file = DatabaseManager.getDatabaseFile(context)!!.parentFile
        val sizeInBytes = file.walk()
            .filter { it.isFile }
            .sumOf { it.length() }

        return Formatter.formatFileSize(context, sizeInBytes)
    }

    fun exportAndShareDatabase(
        context: Context,
        title: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val file = DatabaseManager.getBackupFile(context)
            exportDatabaseState.value = ExportDatabaseState.Writing(file.name)

            DatabaseManager.writeBackupFile(context, file)
            delay(500)
            DatabaseManager.shareBackupFile(context, file, title)

            exportDatabaseState.value = ExportDatabaseState.Idle
        }
    }

    fun updateUpdatePodcastsIntervalMinutesSlider(state: Float) {
        updatePodcastsIntervalMinutesSliderState.floatValue = state
        updatePodcastsIntervalMinutesTranslatedSliderState.intValue =
            UPDATE_PODCASTS_INTERVAL_VALUES[state.roundToInt()]
    }

    fun updateUpdatePodcastsIntervalMinutesSlider(minutes: Int) {
        updatePodcastsIntervalMinutesSliderState.floatValue =
            UPDATE_PODCASTS_INTERVAL_VALUES.indexOfFirst { it >= minutes }.toFloat()
        updatePodcastsIntervalMinutesTranslatedSliderState.intValue = minutes
    }

    fun updateDeleteDownloadsAfterSlider(state: Float) {
        deleteDownloadsAfterSliderState.floatValue = state
        deleteDownloadsAfterTranslatedSliderState.value =
            DeleteDownloadsAfterValues.entries[state.roundToInt()]
    }

    fun updateDeleteDownloadsAfterSlider(seconds: Int) {
        deleteDownloadsAfterTranslatedSliderState.value =
            DeleteDownloadsAfterValues.entries.first { it.seconds == seconds }
        deleteDownloadsAfterSliderState.floatValue =
            deleteDownloadsAfterTranslatedSliderState.value.ordinal.toFloat()
    }

    fun requeueUpdates(
        context: Context
    ) {
        viewModelScope.launch {
            PeriodicPodcastUpdateWorker.enqueueWorker(
                context = context,
                settingsRepository = repository,
                replace = true,
                delay = true
            )
        }
    }

    fun requeueDownloads(
        context: Context,
        db: AppDatabase
    ) {
        viewModelScope.launch {
            DownloadManager.requeueDownloads(
                context = context,
                db = db,
                settingsRepository = repository
            )
        }
    }

}