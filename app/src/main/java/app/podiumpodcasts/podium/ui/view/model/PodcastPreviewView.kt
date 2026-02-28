package app.podiumpodcasts.podium.ui.view.model

import android.util.Log
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FormatColorReset
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.RssFeed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.podiumpodcasts.podium.AppActivity
import app.podiumpodcasts.podium.R
import app.podiumpodcasts.podium.api.db.model.PodcastModel
import app.podiumpodcasts.podium.api.model.PodcastPreviewModel
import app.podiumpodcasts.podium.ui.component.DetailsList
import app.podiumpodcasts.podium.ui.component.DetailsListItemModel
import app.podiumpodcasts.podium.ui.component.common.BackButton
import app.podiumpodcasts.podium.ui.component.layout.ErrorLayout
import app.podiumpodcasts.podium.ui.component.layout.LoadingLayout
import app.podiumpodcasts.podium.ui.component.layout.StateIconLayout
import app.podiumpodcasts.podium.ui.dialog.ShimmerAsyncImage
import app.podiumpodcasts.podium.ui.helper.LocalDatabase
import app.podiumpodcasts.podium.ui.theme.Typography
import app.podiumpodcasts.podium.ui.vm.PodcastPreviewState
import app.podiumpodcasts.podium.ui.vm.PodcastPreviewViewModel
import coil3.compose.AsyncImagePainter
import com.materialkolor.DynamicMaterialExpressiveTheme
import dev.chrisbanes.haze.hazeEffect

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PodcastPreviewView(
    podcast: PodcastPreviewModel,

    onOpenPodcast: (podcast: PodcastModel) -> Unit,
    onBack: () -> Unit
) {
    val db = LocalDatabase.current
    val vm = remember { PodcastPreviewViewModel(db, podcast) }

    val expandedFab by remember { derivedStateOf { vm.listState.firstVisibleItemScrollOffset < 100 } }

    val BACKDROP_SIZE = 200.dp

    LaunchedEffect(vm.duplicate) {
        if(vm.duplicate == null) return@LaunchedEffect

        onBack()
        onOpenPodcast(vm.duplicate!!)
    }

    DynamicMaterialExpressiveTheme(
        seedColor = vm.seedColor ?: MaterialTheme.colorScheme.primary,
        animate = true
    ) {
        Surface(
            Modifier.fillMaxSize()
        ) {
            AnimatedContent(
                targetState = vm.state
            ) { state ->
                when(state) {
                    is PodcastPreviewState.Idle -> {
                        Scaffold(
                            topBar = {
                                TopAppBar(
                                    navigationIcon = {
                                        BackButton(
                                            icon = Icons.Rounded.Close
                                        ) {
                                            onBack()
                                        }
                                    },
                                    colors = TopAppBarDefaults.topAppBarColors(
                                        containerColor = Color.Transparent,
                                        scrolledContainerColor = Color.Transparent
                                    ),
                                    title = { }
                                )
                            },
                            floatingActionButton = {
                                LargeExtendedFloatingActionButton(
                                    expanded = expandedFab,
                                    icon = {
                                        Icon(
                                            modifier = Modifier.size(FloatingActionButtonDefaults.LargeIconSize),
                                            imageVector = Icons.Rounded.Add,
                                            contentDescription = stringResource(R.string.common_action_add)
                                        )
                                    },
                                    text = {
                                        Text(stringResource(R.string.common_action_add))
                                    },
                                    onClick = {
                                        vm.add()
                                    }
                                )
                            },
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        ) { inset ->
                            Box {
                                ShimmerAsyncImage(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(inset.calculateTopPadding() + BACKDROP_SIZE)
                                        .hazeEffect(),

                                    model = podcast.imageUrl,
                                    contentDescription = null,

                                    contentScale = ContentScale.Crop
                                )

                                LazyColumn(
                                    state = vm.listState
                                ) {
                                    item(
                                        key = "BACKDROP"
                                    ) {
                                        Box(
                                            Modifier
                                                .fillMaxWidth()
                                                .height(inset.calculateTopPadding() + BACKDROP_SIZE)
                                                .background(
                                                    Brush.verticalGradient(
                                                        listOf(
                                                            Color.Transparent,
                                                            MaterialTheme.colorScheme.surfaceContainer
                                                        )
                                                    )
                                                )
                                        ) {
                                            ShimmerAsyncImage(
                                                modifier = Modifier
                                                    .align(Alignment.Center)
                                                    .size(128.dp)
                                                    .shadow(
                                                        elevation = 8.dp,
                                                        shape = RoundedCornerShape(16.dp),
                                                        clip = true
                                                    ),

                                                onState = { state ->
                                                    if(state is AsyncImagePainter.State.Success) {
                                                        vm.extractSeedColor(state)
                                                    }
                                                },

                                                model = podcast.imageUrl,
                                                contentDescription = null,

                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                    }

                                    item(
                                        key = "HEADING"
                                    ) {
                                        Surface(
                                            modifier = Modifier.fillMaxWidth(),
                                            color = MaterialTheme.colorScheme.surfaceContainer
                                        ) {
                                            Column {
                                                Spacer(Modifier.height(16.dp))

                                                SelectionContainer(
                                                    Modifier
                                                        .padding(start = 16.dp, end = 16.dp)
                                                ) {
                                                    Column {
                                                        Text(
                                                            text = podcast.title,
                                                            style = Typography.displaySmallEmphasized
                                                        )

                                                        Spacer(Modifier.height(4.dp))

                                                        Text(
                                                            text = podcast.author,
                                                            style = Typography.labelLarge
                                                        )
                                                    }
                                                }

                                                Spacer(Modifier.height(16.dp))
                                            }
                                        }
                                    }

                                    item(
                                        key = "INFO"
                                    ) {
                                        val uriHandler = LocalUriHandler.current

                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .animateItem(),
                                            color = MaterialTheme.colorScheme.surfaceContainer
                                        ) {
                                            Column(
                                                Modifier.padding(16.dp)
                                            ) {
                                                Surface(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(16.dp)),
                                                    color = MaterialTheme.colorScheme.surface,
                                                    contentColor = MaterialTheme.colorScheme.onSurface
                                                ) {
                                                    SelectionContainer {
                                                        Text(
                                                            modifier = Modifier
                                                                .padding(16.dp),
                                                            text = AnnotatedString.fromHtml(
                                                                htmlString = podcast.description
                                                                    .replace("\n", "<br>")
                                                            )
                                                        )
                                                    }
                                                }

                                                Spacer(Modifier.height(16.dp))

                                                DetailsList(
                                                    items = listOf(
                                                        DetailsListItemModel(
                                                            icon = Icons.Rounded.Link,
                                                            label = R.string.common_source,
                                                            value = podcast.link,
                                                            onClick = {
                                                                uriHandler.openUri(podcast.link)
                                                            }
                                                        ),
                                                        DetailsListItemModel(
                                                            icon = Icons.Rounded.RssFeed,
                                                            label = R.string.common_rss_feed,
                                                            value = podcast.fetchUrl
                                                        ),
                                                        DetailsListItemModel(
                                                            icon = Icons.Rounded.Language,
                                                            label = R.string.common_language_code,
                                                            value = podcast.languageCode
                                                        )
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    is PodcastPreviewState.Loading -> {
                        LoadingLayout(
                            size = 96.dp
                        )
                    }

                    is PodcastPreviewState.Done -> {
                        StateIconLayout(
                            icon = Icons.Rounded.Check,
                            contentDescription = stringResource(R.string.common_done)
                        ) {
                            onBack()
                            onOpenPodcast(state.podcast)
                        }
                    }

                    is PodcastPreviewState.Error -> {
                        ErrorLayout {
                            Text(state.reason)
                        }
                    }
                }

            }
        }
    }

    if(vm.showIgnoreSeedColorDialog) AlertDialog(
        onDismissRequest = {
            vm.showIgnoreSeedColorDialog = false
        },

        icon = {
            Icon(
                Icons.Rounded.FormatColorReset,
                stringResource(R.string.dialog_artwork_not_analyzed_yet_title)
            )
        },
        title = {
            Text(stringResource(R.string.dialog_artwork_not_analyzed_yet_title))
        },
        text = {
            Text(stringResource(R.string.dialog_artwork_not_analyzed_yet_text))
        },

        dismissButton = {
            FilledTonalButton(
                onClick = {
                    vm.showIgnoreSeedColorDialog = false
                    vm.add(true)
                }
            ) {
                Text(stringResource(R.string.common_action_continue))
            }
        },

        confirmButton = {
            Button(
                onClick = {
                    vm.showIgnoreSeedColorDialog = false
                }
            ) {
                Text(stringResource(R.string.common_action_wait))
            }
        }
    )
}