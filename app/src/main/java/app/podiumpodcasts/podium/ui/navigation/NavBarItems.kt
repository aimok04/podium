package app.podiumpodcasts.podium.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.School
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import app.podiumpodcasts.podium.R
import kotlinx.serialization.Serializable

@Serializable
enum class NavBarItems(
    val label: Int,
    val icon: ImageVector,
    val navKey: NavKey
) {
    HOME(R.string.route_home, Icons.Rounded.Home, Home),
    DISCOVER(R.string.route_discover, Icons.Rounded.Explore, Discover),
    LIBRARY(R.string.route_library, Icons.Rounded.School, Library)
}