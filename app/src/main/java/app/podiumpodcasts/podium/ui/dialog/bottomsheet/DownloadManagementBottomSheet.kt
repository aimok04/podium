package app.podiumpodcasts.podium.ui.dialog.bottomsheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.DataUsage
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.podiumpodcasts.podium.R
import app.podiumpodcasts.podium.api.db.model.PodcastEpisodeBundle
import app.podiumpodcasts.podium.api.db.model.PodcastEpisodeDownloadState
import app.podiumpodcasts.podium.manager.DownloadManager
import app.podiumpodcasts.podium.ui.component.DetailsList
import app.podiumpodcasts.podium.ui.component.DetailsListItemModel
import app.podiumpodcasts.podium.ui.formatFileSize
import app.podiumpodcasts.podium.ui.formatPubDate
import app.podiumpodcasts.podium.ui.helper.LocalDatabase
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DownloadManagementBottomSheet(
    bundle: PodcastEpisodeBundle,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()

    val context = LocalContext.current
    val db = LocalDatabase.current

    val sheetState = rememberModalBottomSheetState()

    val cachedBundle = remember { mutableStateOf(bundle) }
    LaunchedEffect(bundle) {
        if(bundle.download != null) {
            cachedBundle.value = bundle
        } else {
            sheetState.hide()
            onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = { onDismiss() },
        sheetState = sheetState
    ) {
        val cachedBundle = cachedBundle.value
        if(cachedBundle.download == null) return@ModalBottomSheet

        LazyColumn(
            Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.End
        ) {
            item {
                DetailsList(
                    listOf(
                        DetailsListItemModel(
                            icon = Icons.Rounded.Download,
                            label = R.string.common_state,
                            value = stringResource(
                                cachedBundle.download.parseState().label
                            )
                        ),

                        DetailsListItemModel(
                            icon = Icons.Rounded.DataUsage,
                            label = R.string.common_size,
                            value = when(cachedBundle.download.state) {
                                PodcastEpisodeDownloadState.DOWNLOADED.value ->
                                    formatFileSize(cachedBundle.download.size)

                                else ->
                                    formatFileSize(cachedBundle.download.progress)
                            }
                        ),

                        DetailsListItemModel(
                            icon = Icons.Rounded.CalendarMonth,
                            label = R.string.common_downloaded_at,
                            value = formatPubDate(context, (cachedBundle.download.timestamp / 1000))
                        )
                    )
                )
            }

            item {
                Button(
                    onClick = {
                        scope.launch {
                            DownloadManager.deleteEpisodeDownload(
                                context = context,
                                db = db,
                                episode = bundle.episode
                            )
                        }
                    },
                    shapes = ButtonDefaults.shapes(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Icon(
                        Icons.Rounded.Delete,
                        contentDescription = stringResource(R.string.common_action_delete),
                        modifier = Modifier.size(ButtonDefaults.IconSize),
                    )

                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))

                    Text(stringResource(R.string.common_action_delete))
                }
            }
        }
    }
}