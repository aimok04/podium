package app.podiumpodcasts.podium.ui.view.model

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LoadingIndicatorDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.podiumpodcasts.podium.R
import app.podiumpodcasts.podium.ui.dialog.ShimmerAsyncImage
import app.podiumpodcasts.podium.ui.helper.LocalDatabase
import app.podiumpodcasts.podium.ui.theme.Typography
import app.podiumpodcasts.podium.ui.vm.AddPodcastState
import app.podiumpodcasts.podium.ui.vm.AddPodcastViewModel
import coil3.compose.AsyncImagePainter
import coil3.toBitmap
import com.materialkolor.DynamicMaterialExpressiveTheme
import com.materialkolor.ktx.themeColorOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AddPodcastView(
    predefinedUrl: String? = null,
    onBack: (origin: String) -> Unit
) {
    val scope = rememberCoroutineScope()

    val db = LocalDatabase.current
    val vm = remember { AddPodcastViewModel(db) }

    LaunchedEffect(predefinedUrl) {
        if(predefinedUrl == null) return@LaunchedEffect
        vm.state = AddPodcastState.Loading()

        scope.launch {
            if(predefinedUrl.startsWith("itunes-lookup:")) {
                vm.origin = vm.applePodcastClient.lookup.getRssFeedUrl(
                    id = predefinedUrl.replaceFirst("itunes-lookup:", "")
                )
            } else {
                vm.origin = predefinedUrl
            }

            vm.fetchRssPodcast()
        }
    }

    AnimatedContent(
        targetState = vm.state
    ) { state ->
        when(state) {
            is AddPodcastState.Idle -> if(predefinedUrl == null) Box(
                Modifier
                    .padding(24.dp)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                val focusRequester = remember { FocusRequester() }

                Column(
                    Modifier
                        .widthIn(max = 600.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),

                        value = vm.origin,
                        onValueChange = { value ->
                            vm.origin = value
                        },

                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Language,
                                contentDescription = stringResource(R.string.common_url)
                            )
                        },

                        label = {
                            Text(
                                text = stringResource(R.string.common_url)
                            )
                        },

                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Go
                        ),
                        keyboardActions = KeyboardActions(
                            onGo = { vm.fetchRssPodcast() }
                        )
                    )

                    Spacer(Modifier.height(8.dp))

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { vm.fetchRssPodcast() }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = stringResource(R.string.common_action_continue)
                        )

                        Spacer(Modifier.width(ButtonDefaults.IconSpacing))

                        Text(
                            text = stringResource(R.string.common_action_continue)
                        )
                    }
                }

                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                }
            }

            is AddPodcastState.Loading -> Box(
                Modifier
                    .padding(24.dp)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                ContainedLoadingIndicator(
                    Modifier.size(96.dp)
                )
            }

            is AddPodcastState.Preview -> Box(
                Modifier
                    .padding(24.dp)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ShimmerAsyncImage(
                        modifier = Modifier
                            .size(256.dp)
                            .clip(RoundedCornerShape(16.dp)),

                        model = state.imageUrl,
                        contentDescription = null,

                        onState = { state ->
                            if(state is AsyncImagePainter.State.Success) {
                                scope.launch(Dispatchers.IO) {
                                    val imageBitmap = state.result.image
                                        .toBitmap()
                                        .asImageBitmap()

                                    vm.seedColor = imageBitmap.themeColorOrNull()
                                }
                            }
                        },

                        contentScale = ContentScale.Crop
                    )

                    Spacer(Modifier.height(32.dp))

                    Column(
                        Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.podcast.fetchTitle(),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            style = Typography.displaySmallEmphasized,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = state.podcast.author,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = Typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(Modifier.height(96.dp))

                    DynamicMaterialExpressiveTheme(
                        seedColor = vm.seedColor
                            ?: MaterialTheme.colorScheme.primary
                    ) {
                        FilledIconButton(
                            onClick = { vm.addPodcast() },
                            modifier = Modifier.size(
                                IconButtonDefaults.largeContainerSize(
                                    widthOption = IconButtonDefaults.IconButtonWidthOption.Wide
                                )
                            ),
                            enabled = vm.seedColor != null,
                            shapes = IconButtonDefaults.shapes(
                                shape = IconButtonDefaults.largeSquareShape,
                                pressedShape = IconButtonDefaults.largePressedShape,
                            )
                        ) {
                            Icon(
                                modifier = Modifier.size(
                                    IconButtonDefaults.largeIconSize
                                ),
                                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                contentDescription = stringResource(R.string.common_action_continue)
                            )
                        }
                    }
                }
            }

            is AddPodcastState.Duplicate -> {
                LaunchedEffect(Unit) {
                    onBack(state.duplicate.origin)
                }
            }

            is AddPodcastState.Done -> Box(
                Modifier
                    .padding(24.dp)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                LaunchedEffect(Unit) {
                    scope.launch {
                        delay(500)
                        onBack(vm.origin)
                    }
                }

                Box(
                    Modifier
                        .size(96.dp)
                        .clip(MaterialShapes.Cookie12Sided.toShape())
                        .background(LoadingIndicatorDefaults.containedContainerColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        modifier = Modifier.size(48.dp),
                        imageVector = Icons.Rounded.Check,
                        contentDescription = stringResource(R.string.common_done),
                        tint = LoadingIndicatorDefaults.containedIndicatorColor
                    )
                }
            }

            is AddPodcastState.Error -> Column(
                Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = state.reason
                )
            }
        }
    }
}