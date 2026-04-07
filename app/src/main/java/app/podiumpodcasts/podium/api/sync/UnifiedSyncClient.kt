package app.podiumpodcasts.podium.api.sync

import app.podiumpodcasts.podium.api.sync.gpodder.GpodderClient
import app.podiumpodcasts.podium.api.sync.model.episodeactions.EpisodeAction
import app.podiumpodcasts.podium.api.sync.model.result.AuthResult
import app.podiumpodcasts.podium.api.sync.model.result.EpisodeActionsGetResult
import app.podiumpodcasts.podium.api.sync.model.result.SubscriptionsGetChangesResult
import app.podiumpodcasts.podium.api.sync.model.result.SyncResult
import app.podiumpodcasts.podium.api.sync.model.result.UploadChangesResult
import app.podiumpodcasts.podium.api.sync.nextcloud_gpodder.NextcloudGpodderClient

enum class UnifiedSyncClientType(
    val builder: (
        client: UnifiedSyncClient
    ) -> SyncClient
) {
    GPODDER(
        builder = {
            GpodderClient(
                deviceCaption = it.deviceCaption,
                deviceId = it.deviceId,

                baseUrl = it.baseUrl,
                username = it.username,
                password = it.password,
                cookie = it.cookie
            )
        }
    ),
    NEXTCLOUD_GPODDER(
        builder = {
            NextcloudGpodderClient(
                baseUrl = it.baseUrl,
                username = it.username,
                password = it.password
            )
        }
    )
}

data class UnifiedSyncClient(
    val type: UnifiedSyncClientType,

    val deviceCaption: String,
    val deviceId: String,

    val baseUrl: String,
    val username: String,
    val password: String,
    val cookie: String
) {

    val client = type.builder(this)

    val auth = Auth()
    val episodeActions = EpisodeActions()
    val subscriptions = Subscriptions()

    inner class Auth {
        /**
         * Relogin (only supported in gpodder)
         */
        suspend fun relogin(): SyncResult<AuthResult> {
            return when(client) {
                is GpodderClient -> {
                    client.auth.login()
                }

                else -> {
                    SyncResult.NotSupported()
                }
            }
        }
    }

    inner class EpisodeActions {
        /**
         * Upload episode actions
         *
         * @param actions List of episode actions
         * @return whether update was successful
         */
        suspend fun upload(
            actions: List<EpisodeAction>
        ): SyncResult.Success<UploadChangesResult> {
            return when(client) {
                is GpodderClient -> {
                    client.episodeActions.upload(actions)
                }

                is NextcloudGpodderClient -> {
                    client.episodeActions.upload(actions)
                }

                else -> {
                    throw Exception("client not supported")
                }
            }
        }

        /**
         * Get episode actions
         *
         * @param since UNIX timestamp (seconds)
         * @param aggregated Only return latest actions
         */
        suspend fun get(
            since: Long,
            aggregated: Boolean = true
        ): SyncResult.Success<EpisodeActionsGetResult> {
            return when(client) {
                is GpodderClient -> {
                    client.episodeActions.get(since, aggregated)
                }

                is NextcloudGpodderClient -> {
                    client.episodeActions.get(since)
                }

                else -> {
                    throw Exception("client not supported")
                }
            }
        }
    }

    inner class Subscriptions {
        /**
         * Upload subscriptions
         *
         * @param origins List of subscription URLs
         * @return whether update was successful
         */
        suspend fun upload(
            origins: List<String>
        ): SyncResult.Success<Any> {
            return when(client) {
                is GpodderClient -> {
                    client.subscriptions.upload(origins)
                }

                is NextcloudGpodderClient -> {
                    client.subscriptions.uploadChanges(
                        add = origins,
                        remove = listOf()
                    )

                    SyncResult.Success(Unit)
                }

                else -> {
                    throw Exception("client not supported")
                }
            }
        }

        /**
         * Upload subscription changes
         *
         * @param add List of subscription URLs to add
         * @param remove List of subscription URLs to remove
         * @return whether update was successful
         */
        suspend fun uploadChanges(
            add: List<String>,
            remove: List<String>
        ): SyncResult.Success<UploadChangesResult> {
            return when(client) {
                is GpodderClient -> {
                    client.subscriptions.uploadChanges(
                        add = add,
                        remove = remove
                    )
                }

                is NextcloudGpodderClient -> {
                    client.subscriptions.uploadChanges(
                        add = add,
                        remove = remove
                    )
                }

                else -> {
                    throw Exception("client not supported")
                }
            }
        }

        /**
         * Get subscription changes
         *
         * @param since UNIX timestamp (seconds)
         */
        suspend fun getChanges(
            since: Long
        ): SyncResult.Success<SubscriptionsGetChangesResult> {
            return when(client) {
                is GpodderClient -> {
                    client.subscriptions.getChanges(
                        since = since
                    )
                }

                is NextcloudGpodderClient -> {
                    client.subscriptions.getChanges(
                        since = since
                    )
                }

                else -> {
                    throw Exception("client not supported")
                }
            }
        }
    }

}