package app.podiumpodcasts.podium.ui.vm

import android.content.Context
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import app.podiumpodcasts.podium.api.db.AppDatabase
import app.podiumpodcasts.podium.api.db.model.MediaMetadataExtra
import app.podiumpodcasts.podium.api.db.model.PodcastEpisodeBundle
import app.podiumpodcasts.podium.background.COMMAND_SLEEP_TIMER_GET
import app.podiumpodcasts.podium.background.COMMAND_SLEEP_TIMER_SET
import app.podiumpodcasts.podium.utils.getEpisodeId
import app.podiumpodcasts.podium.utils.getImageSeedColor
import app.podiumpodcasts.podium.utils.getOrigin
import app.podiumpodcasts.podium.utils.isDownload
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

interface SourceTypeState {
    object Unknown : SourceTypeState
    object Streaming : SourceTypeState
    object Downloaded : SourceTypeState
}

class MediaPlayerViewModel : ViewModel() {

    lateinit var db: AppDatabase

    private var mediaController: MediaController? = null

    var isPlayerVisible by mutableStateOf(false)

    var isPlaying by mutableStateOf(false)
    var isLoading by mutableStateOf(false)
    var playbackState by mutableIntStateOf(1)

    var speed by mutableFloatStateOf(1f)

    var lastProgressUpdate by mutableLongStateOf(0L)
    var currentDuration by mutableLongStateOf(1L)
    var currentPosition by mutableLongStateOf(1L)

    // unix millis when sleep timer should trigger
    var sleepTimerTrigger by mutableStateOf<Long?>(null)

    var metadataOrigin by mutableStateOf<String?>(null)
    var metadataEpisodeId by mutableStateOf<String?>(null)
    var metadataImageSeedColor by mutableStateOf<Int?>(null)

    var mediaMetadata by mutableStateOf<MediaMetadata?>(null)

    var mediaSourceType by mutableStateOf<SourceTypeState>(SourceTypeState.Unknown)

    val queue = mutableStateListOf<String>()
    val queueIndex = mutableStateListOf<Int>()

    fun play(
        context: Context,
        bundle: PodcastEpisodeBundle
    ) {
        if(bundle.episode.new) viewModelScope.launch {
            db.podcastEpisodes().unnewAndUpdateNewEpisodesCount(
                origin = bundle.episode.origin,
                episodeId = bundle.episode.id
            )
        }

        val mediaItem = bundle.episode
            .createMediaItem(context)

        mediaController?.setMediaItem(mediaItem, bundle.playState!!.state * 1000L)
        mediaController?.play()
    }

    fun enqueue(
        context: Context,
        bundle: PodcastEpisodeBundle
    ) {
        val mediaItem = bundle.episode
            .createMediaItem(
                context = context,
                resumeAt = bundle.playState?.state?.let { it * 1000L }
            )

        mediaController?.addMediaItem(mediaItem)
        syncQueue()
    }

    fun dequeue(
        episodeId: String
    ) {
        mediaController?.let { mediaController ->
            for(index in 0..mediaController.mediaItemCount) {
                if(index == mediaController.currentMediaItemIndex) continue

                val mediaItem = mediaController.getMediaItemAt(index)
                val mediaItemEpisodeId =
                    mediaItem.mediaMetadata.extras?.getString(MediaMetadataExtra.EPISODE_ID.name)

                if(episodeId != mediaItemEpisodeId) continue

                mediaController.removeMediaItem(index)
                break
            }
        }

        syncQueue()
    }

    fun play() {
        mediaController?.play()
    }

    fun pause() {
        mediaController?.pause()
    }

    fun seek(position: Long) {
        mediaController?.seekTo(position)
    }

    fun replay10() {
        mediaController?.seekTo(mediaController!!.currentPosition - 10 * 1000)
    }

    fun forward10() {
        mediaController?.seekTo(mediaController!!.currentPosition + 10 * 1000)
    }

    fun setPlaybackSpeed(speed: Float) {
        mediaController?.setPlaybackSpeed(speed)
    }

    fun setSleepTimer(sleepTimer: Long?): Boolean {
        val result = mediaController?.sendCustomCommand(
            SessionCommand(COMMAND_SLEEP_TIMER_SET, Bundle().apply {
                putLong("trigger", sleepTimer ?: 0L)
            }),
            Bundle()
        )?.get()

        updateSleepTimer()
        return result?.resultCode == SessionResult.RESULT_SUCCESS
    }

    fun updateSleepTimer() {
        val result = mediaController?.sendCustomCommand(
            SessionCommand(COMMAND_SLEEP_TIMER_GET, Bundle()),
            Bundle()
        )?.get()

        this.sleepTimerTrigger = result?.extras?.getLong("trigger", 0L)
            .takeIf { it != 0L }
    }

    @Composable
    fun getProgressState(): MutableState<Float> {
        val progressState = remember { mutableFloatStateOf(0f) }

        LaunchedEffect(lastProgressUpdate) {
            while(true) {
                val difference = System.currentTimeMillis() - lastProgressUpdate
                val realCurrentPosition = if(isPlaying) {
                    currentPosition + (difference * speed)
                } else {
                    currentPosition
                }

                progressState.value = realCurrentPosition.toFloat() / currentDuration

                delay(100)
            }
        }

        return progressState
    }

    /**
     * Returns sleep timer state (in minutes)
     */
    @Composable
    fun getSleepTimerState(): MutableState<Int?> {
        val sleepTimerState = remember { mutableStateOf<Int?>(null) }

        LaunchedEffect(sleepTimerTrigger) {
            while(true) {
                sleepTimerState.value = sleepTimerTrigger?.let { unixMillis ->
                    val diff = unixMillis - System.currentTimeMillis()
                    ((diff / 1000L) / 60f).roundToInt()
                }

                delay(1000 * 30)
            }
        }

        return sleepTimerState
    }

    fun syncQueue() {
        mediaController?.let { mediaController ->
            queue.clear()
            queueIndex.clear()

            repeat(mediaController.mediaItemCount) { index ->
                if(mediaController.currentMediaItemIndex >= index) return@repeat

                val mediaItem = mediaController.getMediaItemAt(index)
                mediaItem.mediaMetadata.extras?.getString(MediaMetadataExtra.EPISODE_ID.name)
                    ?.let { episodeId ->
                        queue.add(episodeId)
                        queueIndex.add(index)
                    }
            }
        }
    }

    @Composable
    fun rememberMediaItemAt(index: Int): MediaItem? {
        var mediaItem by remember { mutableStateOf<MediaItem?>(null) }
        LaunchedEffect(queueIndex.toList()) {
            mediaItem = mediaController?.getMediaItemAt(index)
        }

        return mediaItem
    }

    val listener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            this@MediaPlayerViewModel.isPlaying = isPlaying
            super.onIsPlayingChanged(isPlaying)
        }

        override fun onEvents(player: Player, events: Player.Events) {
            this@MediaPlayerViewModel.currentDuration = player.duration.coerceAtLeast(1L)
            this@MediaPlayerViewModel.currentPosition = player.currentPosition
            this@MediaPlayerViewModel.lastProgressUpdate = System.currentTimeMillis()
            this@MediaPlayerViewModel.updateSleepTimer()
            super.onEvents(player, events)
        }

        override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
            this@MediaPlayerViewModel.speed = playbackParameters.speed
            super.onPlaybackParametersChanged(playbackParameters)
        }

        override fun onIsLoadingChanged(isLoading: Boolean) {
            this@MediaPlayerViewModel.isLoading = isLoading
            super.onIsLoadingChanged(isLoading)
        }

        override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
            this@MediaPlayerViewModel.mediaMetadata = mediaMetadata
            this@MediaPlayerViewModel.metadataOrigin = mediaMetadata.getOrigin()
            this@MediaPlayerViewModel.metadataEpisodeId = mediaMetadata.getEpisodeId()
            this@MediaPlayerViewModel.metadataImageSeedColor = mediaMetadata.getImageSeedColor()

            this@MediaPlayerViewModel.mediaSourceType = when(mediaMetadata.isDownload()) {
                true -> SourceTypeState.Downloaded
                false -> SourceTypeState.Streaming
            }

            syncQueue()

            super.onMediaMetadataChanged(mediaMetadata)
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            this@MediaPlayerViewModel.playbackState = playbackState
            this@MediaPlayerViewModel.isPlayerVisible = playbackState != Player.STATE_IDLE

            super.onPlaybackStateChanged(playbackState)
        }
    }

    fun registerMediaController(mediaController: MediaController) {
        this@MediaPlayerViewModel.mediaController = mediaController

        this@MediaPlayerViewModel.isPlaying = mediaController.isPlaying

        this@MediaPlayerViewModel.currentDuration = mediaController.duration.coerceAtLeast(1L)
        this@MediaPlayerViewModel.currentPosition = mediaController.currentPosition
        this@MediaPlayerViewModel.lastProgressUpdate = System.currentTimeMillis()

        this@MediaPlayerViewModel.speed = mediaController.playbackParameters.speed

        this@MediaPlayerViewModel.isLoading = mediaController.isLoading

        this@MediaPlayerViewModel.mediaMetadata = mediaController.mediaMetadata
        mediaController.currentMediaItem?.let { mediaItem ->
            this@MediaPlayerViewModel.metadataOrigin = mediaItem.getOrigin()
            this@MediaPlayerViewModel.metadataEpisodeId = mediaItem.getEpisodeId()
            this@MediaPlayerViewModel.metadataImageSeedColor = mediaItem.getImageSeedColor()

            this@MediaPlayerViewModel.mediaSourceType = when(mediaItem.isDownload()) {
                true -> SourceTypeState.Downloaded
                false -> SourceTypeState.Streaming
            }
        }

        this@MediaPlayerViewModel.playbackState = mediaController.playbackState
        this@MediaPlayerViewModel.isPlayerVisible =
            mediaController.playbackState != Player.STATE_IDLE

        syncQueue()

        mediaController.addListener(listener)
    }

    fun unregisterMediaController() {
        mediaController?.removeListener(listener)
    }

    fun passDB(db: AppDatabase) {
        this.db = db
    }

    fun getMediaItem(index: Int): MediaItem? {
        return mediaController?.getMediaItemAt(index)
    }

}