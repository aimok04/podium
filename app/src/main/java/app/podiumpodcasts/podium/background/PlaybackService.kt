package app.podiumpodcasts.podium.background

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import androidx.annotation.OptIn
import androidx.core.content.IntentCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.CommandButton
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import app.podiumpodcasts.podium.R
import app.podiumpodcasts.podium.background.notification.NewPodcastEpisodeNotification
import app.podiumpodcasts.podium.manager.DatabaseManager
import app.podiumpodcasts.podium.ui.DeepLink
import app.podiumpodcasts.podium.ui.asPendingIntent
import app.podiumpodcasts.podium.utils.getEpisodeId
import app.podiumpodcasts.podium.utils.getOrigin
import app.podiumpodcasts.podium.utils.getResumeAt
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

const val COMMAND_SLEEP_TIMER_SET = "app.podiumpodcasts.podium.COMMAND_SLEEP_TIMER_SET"
const val COMMAND_SLEEP_TIMER_GET = "app.podiumpodcasts.podium.COMMAND_SLEEP_TIMER_GET"
const val COMMAND_CYCLE_SPEED = "app.podiumpodcasts.podium.COMMAND_CYCLE_SPEED"

class PlaybackService : MediaSessionService() {

    val db by lazy {
        DatabaseManager.build(this)
    }

    private val job = SupervisorJob()
    private val scope = CoroutineScope(job)

    private var mediaSession: MediaSession? = null

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
    private var sleepTimerTrigger: Long? = null
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

        val player = ExoPlayer.Builder(this)
            .setSeekBackIncrementMs(10000)
            .setSeekForwardIncrementMs(30000)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
                    .build(),
                true
            )
            .setHandleAudioBecomingNoisy(true)
            .build()

        mediaSession = MediaSession.Builder(this, player)
            .setMediaButtonPreferences(buildMediaButtons())
            .setSessionActivity(
                DeepLink.OpenMediaPlayer()
                    .asPendingIntent(this, 42)!!
            )
            .setCallback(SessionCallback(this))
            .build()

        registerPlayStateListener()
    }

    fun buildMediaButtons(): List<CommandButton> {
        val seekBack = CommandButton.Builder(CommandButton.ICON_SKIP_BACK_10)
            .setPlayerCommand(Player.COMMAND_SEEK_BACK)
            .setDisplayName("Rewind")
            .build()

        val seekForward = CommandButton.Builder(CommandButton.ICON_SKIP_FORWARD_30)
            .setPlayerCommand(Player.COMMAND_SEEK_FORWARD)
            .setDisplayName("Skip")
            .build()

        val playbackSpeed = CommandButton.Builder(CommandButton.ICON_PLAYBACK_SPEED)
            .setSessionCommand(SessionCommand(COMMAND_CYCLE_SPEED, Bundle.EMPTY))
            .setDisplayName("Playback Speed")
            .build()

        return listOf(
            seekBack,
            seekForward,
            playbackSpeed
        )
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    @UnstableApi
    private class SessionCallback(
        val service: PlaybackService
    ) : MediaSession.Callback {
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {
            val sessionCommands = MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS.buildUpon()
                .add(SessionCommand(COMMAND_SLEEP_TIMER_SET, Bundle.EMPTY))
                .add(SessionCommand(COMMAND_SLEEP_TIMER_GET, Bundle.EMPTY))
                .add(SessionCommand(COMMAND_CYCLE_SPEED, Bundle.EMPTY))
                .build()
            return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
                .setAvailableSessionCommands(sessionCommands)
                .build()
        }

        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle
        ): ListenableFuture<SessionResult> {
            when(customCommand.customAction) {

                COMMAND_SLEEP_TIMER_SET -> {
                    val trigger = customCommand.customExtras.getLong("trigger")

                    if(trigger != 0L) {
                        service.startSleepTimer(
                            trigger = trigger
                        )
                    } else {
                        service.stopSleepTimer()
                    }

                    return Futures.immediateFuture(
                        SessionResult(SessionResult.RESULT_SUCCESS)
                    )
                }

                COMMAND_SLEEP_TIMER_GET -> {
                    val extras = Bundle().apply {
                        putLong("trigger", service.sleepTimerTrigger ?: 0L)
                    }

                    return Futures.immediateFuture(
                        SessionResult(SessionResult.RESULT_SUCCESS, extras)
                    )
                }

                COMMAND_CYCLE_SPEED -> {
                    val currentSpeed = session.player.playbackParameters.speed
                    val nextSpeed = when {
                        currentSpeed < 1.5f -> 1.5f
                        currentSpeed < 2.0f -> 2.0f
                        else -> 1.0f
                    }
                    session.player.setPlaybackSpeed(nextSpeed)

                    return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                }

                else -> return super.onCustomCommand(session, controller, customCommand, args)

            }
        }

        /**
         * Remap skip backward and skip forward to seek back and seek forward
         * To allow seeking using headphone buttons for example
         */
        override fun onMediaButtonEvent(
            session: MediaSession,
            controllerInfo: MediaSession.ControllerInfo,
            intent: Intent
        ): Boolean {
            IntentCompat.getParcelableExtra(intent, Intent.EXTRA_KEY_EVENT, KeyEvent::class.java)
                ?.let { keyEvent ->
                    if(keyEvent.action != KeyEvent.ACTION_UP) return@let

                    when(keyEvent.keyCode) {
                        KeyEvent.KEYCODE_MEDIA_SKIP_BACKWARD,
                        KeyEvent.KEYCODE_MEDIA_PREVIOUS -> {
                            return true
                        }

                        KeyEvent.KEYCODE_MEDIA_SKIP_FORWARD,
                        KeyEvent.KEYCODE_MEDIA_NEXT -> {
                            session.player.seekForward()
                            return true
                        }
                    }
                }

            return super.onMediaButtonEvent(session, controllerInfo, intent)
        }

        /**
         * Remap COMMAND_SEEK_TO_PREVIOUS and COMMAND_SEEK_TO_NEXT to seek back and seek forward
         * To allow seeking using headphone buttons for example
         */
        override fun onPlayerCommandRequest(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            playerCommand: Int
        ): Int {
            if(playerCommand == Player.COMMAND_SEEK_TO_PREVIOUS) {
                session.player.seekBack()
                return SessionResult.RESULT_INFO_SKIPPED
            } else if(playerCommand == Player.COMMAND_SEEK_TO_NEXT) {
                session.player.seekForward()
                return SessionResult.RESULT_INFO_SKIPPED
            }

            return super.onPlayerCommandRequest(session, controller, playerCommand)
        }
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

    private fun stopSleepTimer() {
        sleepTimerTrigger = null
        sleepTimerRunnable.let { sleepTimerHandler.removeCallbacks(it) }
    }

    private fun startSleepTimer(trigger: Long) {
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