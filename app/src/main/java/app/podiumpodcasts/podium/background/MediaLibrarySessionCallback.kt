package app.podiumpodcasts.podium.background

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import androidx.core.content.IntentCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaConstants
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaLibraryService.MediaLibrarySession
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import app.podiumpodcasts.podium.R
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.guava.future
import kotlinx.coroutines.plus

enum class LibraryTabs(
    val id: String,
    val titleId: Int
) {
    CONTINUE_PLAYING("tab:continue_playing", R.string.route_continue_playing),
    NEW_EPISODES("tab:new_episodes", R.string.route_new_episodes),
    SUBSCRIPTIONS("tab:subscriptions", R.string.route_subscriptions),
    LOCALLY_AVAILABLE("tab:locally_available", R.string.route_locally_available);

    fun toMediaItem(
        context: Context
    ): MediaItem {
        return MediaItem.Builder()
            .setMediaId(id)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(context.getString(titleId))
                    .setIsBrowsable(true)
                    .setIsPlayable(false)
                    .setMediaType(MediaMetadata.MEDIA_TYPE_FOLDER_PODCASTS)
                    .build()
            )
            .build()
    }
}

@UnstableApi
class MediaLibrarySessionCallback(
    val service: PlaybackService
) : MediaLibrarySession.Callback {

    private val scope = CoroutineScope(Dispatchers.Main) + Job()

    override fun onConnect(
        session: MediaSession,
        controller: MediaSession.ControllerInfo
    ): MediaSession.ConnectionResult {
        val sessionCommands = MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS.buildUpon()
            .add(SessionCommand(COMMAND_SLEEP_TIMER_SET, Bundle.EMPTY))
            .add(SessionCommand(COMMAND_SLEEP_TIMER_GET, Bundle.EMPTY))
            .add(SessionCommand(COMMAND_CYCLE_SPEED, Bundle.EMPTY))
            .add(SessionCommand(COMMAND_SET_SEEK_BACK_INCREMENT, Bundle.EMPTY))
            .add(SessionCommand(COMMAND_SET_SEEK_FORWARD_INCREMENT, Bundle.EMPTY))
            .add(SessionCommand.COMMAND_CODE_LIBRARY_GET_CHILDREN)
            .add(SessionCommand.COMMAND_CODE_LIBRARY_GET_ITEM)
            .add(SessionCommand.COMMAND_CODE_LIBRARY_GET_LIBRARY_ROOT)
            .build()

        return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
            .setAvailableSessionCommands(sessionCommands)
            .setAvailablePlayerCommands(MediaSession.ConnectionResult.DEFAULT_PLAYER_COMMANDS)
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

            COMMAND_SET_SEEK_BACK_INCREMENT -> {
                val increment = customCommand.customExtras.getLong("increment")
                (session.player as? ExoPlayer)?.setSeekBackIncrementMs(increment)

                return Futures.immediateFuture(
                    SessionResult(SessionResult.RESULT_SUCCESS)
                )
            }

            COMMAND_SET_SEEK_FORWARD_INCREMENT -> {
                val increment = customCommand.customExtras.getLong("increment")
                (session.player as? ExoPlayer)?.setSeekForwardIncrementMs(increment)

                return Futures.immediateFuture(
                    SessionResult(SessionResult.RESULT_SUCCESS)
                )
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

    override fun onGetLibraryRoot(
        session: MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        params: MediaLibraryService.LibraryParams?
    ): ListenableFuture<LibraryResult<MediaItem>> {
        val extras = Bundle()
        extras.putInt(
            MediaConstants.EXTRAS_KEY_CONTENT_STYLE_BROWSABLE,
            MediaConstants.EXTRAS_VALUE_CONTENT_STYLE_GRID_ITEM
        )
        extras.putInt(
            MediaConstants.EXTRAS_KEY_CONTENT_STYLE_PLAYABLE,
            MediaConstants.EXTRAS_VALUE_CONTENT_STYLE_CATEGORY_LIST_ITEM
        )

        return Futures.immediateFuture(
            LibraryResult.ofItem(
                MediaItem.Builder()
                    .setMediaId("root")
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setIsPlayable(false)
                            .setIsBrowsable(false)
                            .setMediaType(MediaMetadata.MEDIA_TYPE_FOLDER_MIXED)
                            .build()
                    )
                    .build(),
                MediaLibraryService.LibraryParams.Builder()
                    .setExtras(extras)
                    .build()
            )
        )
    }

    override fun onGetChildren(
        session: MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        parentId: String,
        page: Int,
        pageSize: Int,
        params: MediaLibraryService.LibraryParams?
    ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
        return scope.future(Dispatchers.IO) {
            val items = when(parentId) {
                "root" -> {
                    LibraryTabs.entries.map {
                        it.toMediaItem(service)
                    }
                }

                LibraryTabs.CONTINUE_PLAYING.id -> {
                    service.db.podcastEpisodePlayStates()
                        .getContinuePlayingSync(offset = page * pageSize, limit = pageSize)
                        .map { it.toPodcastEpisodeBundle().createMediaItem(service) }
                }

                LibraryTabs.NEW_EPISODES.id -> {
                    service.db.podcastEpisodes()
                        .getNew(offset = page * pageSize, limit = pageSize)
                        .map { it.createMediaItem(service) }
                }

                LibraryTabs.SUBSCRIPTIONS.id -> {
                    service.db.podcastSubscriptions()
                        .getByNewEpisodes(offset = page * pageSize, limit = pageSize)
                        .map { it.createMediaItem() }
                }

                LibraryTabs.LOCALLY_AVAILABLE.id -> {
                    service.db.podcasts()
                        .get(offset = page * pageSize, limit = pageSize)
                        .map { it.createMediaItem() }
                }

                else -> {
                    if(parentId.startsWith("podcast:")) {
                        val origin = parentId.removePrefix("podcast:")

                        service.db.podcastEpisodes()
                            .get(origin = origin, offset = page * pageSize, limit = pageSize)
                            .map { it.createMediaItem(service) }
                    } else {
                        listOf()
                    }
                }
            }

            LibraryResult.ofItemList(ImmutableList.copyOf(items), params)
        }
    }

    override fun onAddMediaItems(
        mediaSession: MediaSession,
        controller: MediaSession.ControllerInfo,
        mediaItems: List<MediaItem>
    ): ListenableFuture<List<MediaItem>> {
        return scope.future(Dispatchers.IO) {
            mediaItems.map { item ->
                if(item.localConfiguration != null) return@map item
                if(!item.mediaId.startsWith("episode:")) return@map item

                val episodeId = item.mediaId.replaceFirst("episode:", "")
                val bundle = service.db.podcastEpisodes().get(episodeId).first()

                bundle.createMediaItem(service)
            }
        }
    }

    override fun onSetMediaItems(
        mediaSession: MediaSession,
        controller: MediaSession.ControllerInfo,
        mediaItems: List<MediaItem>,
        startIndex: Int,
        startPositionMs: Long
    ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> {
        return scope.future(Dispatchers.IO) {
            MediaSession.MediaItemsWithStartPosition(
                mediaItems.map { item ->
                    if(item.localConfiguration != null) return@map item
                    if(!item.mediaId.startsWith("episode:")) return@map item

                    val episodeId = item.mediaId.replaceFirst("episode:", "")
                    val bundle = service.db.podcastEpisodes().get(episodeId).first()

                    bundle.createMediaItem(service)
                },
                1,
                0L
            )
        }
    }
}