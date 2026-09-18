package com.nikpapajohn.moviedb.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
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
    val icon: ImageVector,
    val labelRes: Int,
) {
    HOME(Destination.Home, Destination.Home::class, Icons.Filled.Home, R.string.nav_home),
    FAVORITES(Destination.Favorites, Destination.Favorites::class, Icons.Filled.Bookmark, R.string.nav_favorites),
    PROFILE(Destination.Profile, Destination.Profile::class, Icons.Outlined.Person, R.string.nav_profile),
}
