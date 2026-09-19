package com.nikpapajohn.moviedb.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.ui.graphics.vector.ImageVector
import com.nikpapajohn.moviedb.R
import kotlin.reflect.KClass
import kotlinx.serialization.Serializable

/** Type-safe Navigation Compose routes: no string building, no manual argument parsing. */
sealed interface Destination {
    @Serializable
    data object Home : Destination

    @Serializable
    data object Favorites : Destination

    @Serializable
    data object Profile : Destination

    @Serializable
    data object About : Destination

    @Serializable
    data class Details(val movieId: Int) : Destination
}

enum class BottomTab(
    val destination: Destination,
    val route: KClass<out Destination>,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val labelRes: Int,
) {
    HOME(
        destination = Destination.Home,
        route = Destination.Home::class,
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
        labelRes = R.string.nav_home,
    ),
    FAVORITES(
        destination = Destination.Favorites,
        route = Destination.Favorites::class,
        selectedIcon = Icons.Filled.Bookmark,
        unselectedIcon = Icons.Outlined.BookmarkBorder,
        labelRes = R.string.nav_favorites,
    ),
    PROFILE(
        destination = Destination.Profile,
        route = Destination.Profile::class,
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person,
        labelRes = R.string.nav_profile,
    ),
}
