package com.nikpapajohn.moviedb.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.nikpapajohn.moviedb.R
import com.nikpapajohn.moviedb.ui.about.AboutRoute
import com.nikpapajohn.moviedb.ui.details.DetailsRoute
import com.nikpapajohn.moviedb.ui.favorites.FavoritesRoute
import com.nikpapajohn.moviedb.ui.navigation.BottomTab
import com.nikpapajohn.moviedb.ui.navigation.Destination
import com.nikpapajohn.moviedb.ui.popular.PopularRoute
import com.nikpapajohn.moviedb.ui.profile.ProfileRoute
import kotlinx.coroutines.launch

@Composable
fun MovieDbApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Details and About are full-screen destinations: no bottom bar, no drawer gesture.
    val selectedTab = BottomTab.entries.firstOrNull { tab ->
        currentDestination?.hierarchy?.any { it.hasRoute(tab.route) } == true
    }

    fun closeDrawer() = scope.launch { drawerState.close() }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = selectedTab != null,
        drawerContent = {
            AppDrawer(
                selectedTab = selectedTab,
                onTabClick = { tab ->
                    closeDrawer()
                    navController.navigateToTab(tab)
                },
                onAboutClick = {
                    closeDrawer()
                    navController.navigate(Destination.About)
                },
            )
        },
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            // Zero here, because every screen owns a top bar that draws behind the status bar.
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                if (selectedTab != null) {
                    NavigationBar {
                        BottomTab.entries.forEach { tab ->
                            NavigationBarItem(
                                selected = tab == selectedTab,
                                onClick = {
                                    if (tab != selectedTab) navController.navigateToTab(tab)
                                },
                                icon = { Icon(tab.icon, contentDescription = null) },
                                label = { Text(stringResource(tab.labelRes)) },
                            )
                        }
                    }
                }
            },
        ) { innerPadding ->
            val openDrawer: () -> Unit = { scope.launch { drawerState.open() } }

            NavHost(
                navController = navController,
                startDestination = Destination.Home,
                modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding()),
            ) {
                composable<Destination.Home> {
                    PopularRoute(
                        onNavigateToDetails = { navController.navigate(Destination.Details(it)) },
                        onNavigateToFavorites = { navController.navigateToTab(BottomTab.FAVORITES) },
                        onMenuClick = openDrawer,
                    )
                }
                composable<Destination.Favorites> {
                    FavoritesRoute(
                        onNavigateToDetails = { navController.navigate(Destination.Details(it)) },
                        onMenuClick = openDrawer,
                    )
                }
                composable<Destination.Profile> {
                    ProfileRoute(onMenuClick = openDrawer)
                }
                composable<Destination.About> {
                    AboutRoute(onNavigateBack = { navController.popBackStack() })
                }
                composable<Destination.Details> {
                    // movieId is read from SavedStateHandle inside the ViewModel.
                    DetailsRoute(onNavigateBack = { navController.popBackStack() })
                }
            }
        }
    }
}

private fun NavHostController.navigateToTab(tab: BottomTab) {
    navigate(tab.destination) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
private fun AppDrawer(
    selectedTab: BottomTab?,
    onTabClick: (BottomTab) -> Unit,
    onAboutClick: () -> Unit,
) {
    ModalDrawerSheet {
        Surface(color = MaterialTheme.colorScheme.primary) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 32.dp, bottom = 20.dp),
            ) {
                Text(
                    text = stringResource(R.string.home_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
                Text(
                    text = stringResource(R.string.home_welcome_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                )
            }
        }

        BottomTab.entries.forEach { tab ->
            NavigationDrawerItem(
                label = { Text(stringResource(tab.labelRes)) },
                icon = { Icon(tab.icon, contentDescription = null) },
                selected = tab == selectedTab,
                onClick = { onTabClick(tab) },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
            )
        }

        HorizontalDivider(modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp))

        NavigationDrawerItem(
            label = { Text(stringResource(R.string.nav_about)) },
            icon = { Icon(Icons.Outlined.Info, contentDescription = null) },
            selected = false,
            onClick = onAboutClick,
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
        )
    }
}
