package app.podiumpodcasts.podium.ui.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldState
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.NavKey
import app.podiumpodcasts.podium.ui.helper.LocalSettingsRepository

@Composable
fun NavBarScaffold(
    layoutType: NavigationSuiteType =
        NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(currentWindowAdaptiveInfo()),
    state: NavigationSuiteScaffoldState,

    currentNavKey: @Composable () -> NavKey,
    onClickItem: (item: NavBarItems) -> Unit,
    content: @Composable () -> Unit
) {
    val currentNavKey = currentNavKey()

    val settingsRepository = LocalSettingsRepository.current
    val disableApplePodcastsApi = settingsRepository.privacy.disableApplePodcastsApi
        .collectAsState(false)

    NavigationSuiteScaffold(
        layoutType = layoutType,
        state = state,
        containerColor = Color.Transparent,
        navigationSuiteItems = {
            NavBarItems.entries.forEach {
                if(disableApplePodcastsApi.value && it == NavBarItems.DISCOVER)
                    return@forEach

                item(
                    icon = {
                        Icon(
                            it.icon,
                            contentDescription = stringResource(it.label)
                        )
                    },
                    label = { Text(stringResource(it.label)) },
                    selected = currentNavKey == it.navKey,
                    onClick = {
                        onClickItem(it)
                    }
                )
            }
        },
        content = content
    )
}