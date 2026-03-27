package app.podiumpodcasts.podium.background

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.CommandButton
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import app.podiumpodcasts.podium.R
import app.podiumpodcasts.podium.SettingsRepository
import app.podiumpodcasts.podium.background.notification.NewPodcastEpisodeNotification
import app.podiumpodcasts.podium.manager.DatabaseManager
import app.podiumpodcasts.podium.ui.DeepLink
import app.podiumpodcasts.podium.ui.asPendingIntent
import app.podiumpodcasts.podium.utils.getEpisodeId
import app.podiumpodcasts.podium.utils.getOrigin
import app.podiumpodcasts.podium.utils.getResumeAt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

const val COMMAND_SLEEP_TIMER_SET = "app.podiumpodcasts.podium.COMMAND_SLEEP_TIMER_SET"
const val COMMAND_SLEEP_TIMER_GET = "app.podiumpodcasts.podium.COMMAND_SLEEP_TIMER_GET"
const val COMMAND_CYCLE_SPEED = "app.podiumpodcasts.podium.COMMAND_CYCLE_SPEED"

const val COMMAND_SET_SEEK_BACK_INCREMENT =
    "app.podiumpodcasts.podium.COMMAND_SET_SEEK_BACK_INCREMENT"
const val COMMAND_SET_SEEK_FORWARD_INCREMENT =
    "app.podiumpodcasts.podium.COMMAND_SET_SEEK_FORWARD_INCREMENT"

class PlaybackService : MediaLibraryService() {

    val db by lazy {
        DatabaseManager.build(this)
    }

    private val settingsRepository = SettingsRepository(this)

    private val job = SupervisorJob()
    private val scope = CoroutineScope(job)

    private var mediaSession: MediaLibrarySession? = null

    private var currentSkipEndingValue: Int = 0

    private var updatePlayStateHandler: Handler = Handler(Looper.getMainLooper())
    private var updatePlayStateRunnable = Runnable {
        if(mediaSession?.player?.isPlaying != true) return@Runnable
        enqueueUpdatePlayState()
        updatePlayState()
    }

    private var skipEndingHandler: Handler = Handler(Looper.getMainLooper())
    private var skipEndingRunnable = Runnable {
        if(mediaSession?.player?.isPlaying != true) return@Runnable
        enqueueSkipEnding()
        checkSkipEnding()
    }

    private var sleepTimerHandler: Handler = Handler(Looper.getMainLooper())
    var sleepTimerTrigger: Long? = null
    private var sleepTimerRunnable = Runnable {
        sleepTimerTrigger = null
        mediaSession?.player?.pause()
    }

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()

        setMediaNotificationProvider(
            DefaultMediaNotificationProvider.Builder(this).build()
                .apply {
                    setSmallIcon(
                        R.drawable.ic_notification_icon
                    )
                }
        )

        val seekBackIncrement = runBlocking {
            settingsRepository.behavior.playerSeekBackIncrement.first()
        }

        val seekForwardIncrement = runBlocking {
            settingsRepository.behavior.playerSeekForwardIncrement.first()
        }

        val player = ExoPlayer.Builder(this)
            .setSeekBackIncrementMs(seekBackIncrement)
            .setSeekForwardIncrementMs(seekForwardIncrement)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
                    .build(),
                true
            )
            .setHandleAudioBecomingNoisy(true)
            .build()

        mediaSession = MediaLibrarySession.Builder(this, player, MediaLibrarySessionCallback(this))
            .setMediaButtonPreferences(buildMediaButtons(player))
            .setSessionActivity(
                DeepLink.OpenMediaPlayer()
                    .asPendingIntent(this, 42)!!
            )
            .build()

        registerPlayStateListener()
    }

    fun buildMediaButtons(
        player: ExoPlayer
    ): List<CommandButton> {
        val seekBack = CommandButton.Builder(
            when(player.seekBackIncrement) {
                5000L -> CommandButton.ICON_SKIP_BACK_5
                10000L -> CommandButton.ICON_SKIP_FORWARD_10
                15000L -> CommandButton.ICON_SKIP_FORWARD_15
                30000L -> CommandButton.ICON_SKIP_FORWARD_30
                else -> CommandButton.ICON_SKIP_FORWARD
            }
        )
            .setPlayerCommand(Player.COMMAND_SEEK_BACK)
            .setCustomIconResId(
                when(player.seekBackIncrement) {
                    5000L -> R.drawable.ic_replay_5
                    10000L -> R.drawable.ic_replay_10
                    30000L -> R.drawable.ic_replay_30
                    else -> R.drawable.ic_replay
                }
            )
            .setDisplayName(getString(R.string.common_action_seek_back))
            .build()

        val seekForward = CommandButton.Builder(
            when(player.seekForwardIncrement) {
                5000L -> CommandButton.ICON_SKIP_FORWARD_5
                10000L -> CommandButton.ICON_SKIP_FORWARD_10
                15000L -> CommandButton.ICON_SKIP_FORWARD_15
                30000L -> CommandButton.ICON_SKIP_FORWARD_30
                else -> CommandButton.ICON_SKIP_FORWARD
            }
        )
            .setPlayerCommand(Player.COMMAND_SEEK_FORWARD)
            .setCustomIconResId(
                when(player.seekForwardIncrement) {
                    5000L -> R.drawable.ic_forward_5
                    10000L -> R.drawable.ic_forward_10
                    30000L -> R.drawable.ic_forward_30
                    else -> R.drawable.ic_forward
                }
            )
            .setDisplayName(getString(R.string.common_action_seek_forward))
            .build()

        val playbackSpeed = CommandButton.Builder(CommandButton.ICON_PLAYBACK_SPEED)
            .setSessionCommand(SessionCommand(COMMAND_CYCLE_SPEED, Bundle.EMPTY))
            .setCustomIconResId(R.drawable.ic_playback_speed)
            .setDisplayName(getString(R.string.common_action_toggle_playback_speed))
            .build()

        return listOf(
            seekBack,
            seekForward,
            playbackSpeed
        )
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? {
        return mediaSession
    }

    private fun enqueueUpdatePlayState() {
        updatePlayStateHandler.postDelayed(updatePlayStateRunnable, 20 * 1000)
    }

    private fun updatePlayState() {
        val episodeId = getEpisodeId() ?: return
        val currentPosition = mediaSession?.player?.currentPosition ?: return
        val duration = mediaSession?.player?.duration ?: return

        if(duration < 1000) return

        val state = (currentPosition / 1000).toInt()
        val remainingSeconds = (duration / 1000) - (currentPosition / 1000)

        scope.launch {
            if(remainingSeconds < 90) {
                db.podcastEpisodePlayStates()
                    .savePlayed(
                        episodeId = episodeId,
                        played = true
                    )
            }

            db.podcastEpisodePlayStates()
                .saveState(
                    episodeId = episodeId,
                    state = state.coerceAtLeast(1)
                )
        }
    }

    private fun enqueueSkipEnding() {
        skipEndingHandler.postDelayed(skipEndingRunnable, 1000)
    }

    private fun checkSkipEnding() {
        val player = mediaSession?.player ?: return
        if(currentSkipEndingValue == 0) return

        val diff = (player.duration) - (currentSkipEndingValue * 1000L)

        if(player.currentPosition < diff) return
        mediaSession?.player?.seekTo(player.duration)
    }

    private fun getOrigin() = mediaSession?.player?.currentMediaItem?.getOrigin()

    private fun getEpisodeId() = mediaSession?.player?.currentMediaItem?.getEpisodeId()

    private fun registerPlayStateListener() {
        mediaSession?.player?.addListener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                mediaItem?.getResumeAt()?.let { resumeAt ->
                    mediaSession?.player?.seekTo(resumeAt)
                }

                super.onMediaItemTransition(mediaItem, reason)
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                updatePlayState()
                if(playbackState == Player.STATE_ENDED) {
                    getEpisodeId()?.let { episodeId ->
                        scope.launch {
                            db.podcastEpisodePlayStates()
                                .savePlayed(
                                    episodeId = episodeId,
                                    played = true
                                )
                        }
                    }
                }

                super.onPlaybackStateChanged(playbackState)
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updatePlayState()

                if(isPlaying) {
                    enqueueUpdatePlayState()
                    enqueueSkipEnding()
                } else {
                    updatePlayStateHandler.removeCallbacks(updatePlayStateRunnable)
                    skipEndingHandler.removeCallbacks(skipEndingRunnable)
                }

                super.onIsPlayingChanged(isPlaying)
            }

            override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
                val player = mediaSession?.player

                getOrigin()?.let { origin ->
                    getEpisodeId()?.let { episodeId ->
                        NewPodcastEpisodeNotification
                            .cancel(this@PlaybackService.applicationContext, episodeId)

                        scope.launch {
                            val podcast = db.podcasts().getSync(origin)
                            currentSkipEndingValue = (podcast?.skipEnding) ?: 0

                            podcast?.let { podcast ->
                                withContext(Dispatchers.Main) {
                                    // skip beginning if defined for podcast
                                    if(podcast.skipBeginning == 0) return@withContext

                                    if(player == null) return@withContext
                                    if(player.currentPosition > 3000L) return@withContext
                                    player.seekTo(podcast.skipBeginning * 1000L)
                                }
                            }

                            val last = db.podcastHistory().getLast()
                                .first()

                            // don't save duplicate elements if playback happened in the 4 hours
                            if((last?.episode?.id == episodeId)) {
                                val timestamp = System.currentTimeMillis()
                                if((timestamp - last.history.timestamp) < 1000 * 60 * 60 * 4) {
                                    db.podcastHistory().updateTimestamp(
                                        episodeId = episodeId,
                                        timestamp = timestamp
                                    )
                                    return@launch
                                }
                            }

                            db.podcastHistory().insert(
                                origin = origin,
                                episodeId = episodeId
                            )
                        }
                    }
                }

                super.onMediaMetadataChanged(mediaMetadata)
            }
        })
    }

    fun stopSleepTimer() {
        sleepTimerTrigger = null
        sleepTimerRunnable.let { sleepTimerHandler.removeCallbacks(it) }
    }

    fun startSleepTimer(trigger: Long) {
        sleepTimerTrigger = trigger

        val delay = trigger - System.currentTimeMillis()

        sleepTimerRunnable.let { sleepTimerHandler.removeCallbacks(it) }
        sleepTimerHandler.postDelayed(sleepTimerRunnable, delay)
    }

    override fun onDestroy() {
        updatePlayStateHandler.removeCallbacks(updatePlayStateRunnable)
        skipEndingHandler.removeCallbacks(skipEndingRunnable)
        sleepTimerHandler.removeCallbacksAndMessages(null)

        stopSleepTimer()

        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}