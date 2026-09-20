package com.nikpapajohn.moviedb.ui.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nikpapajohn.moviedb.R
import com.nikpapajohn.moviedb.domain.model.Movie
import com.nikpapajohn.moviedb.domain.model.MovieListItem
import com.nikpapajohn.moviedb.ui.ObserveEffects
import com.nikpapajohn.moviedb.ui.components.EmptyState
import com.nikpapajohn.moviedb.ui.components.MovieCard
import com.nikpapajohn.moviedb.ui.components.AppSnackbarHost
import com.nikpapajohn.moviedb.ui.components.SnackbarMessage
import com.nikpapajohn.moviedb.ui.components.SnackbarRequest
import com.nikpapajohn.moviedb.ui.components.rememberSnackbarController
import com.nikpapajohn.moviedb.ui.favorites.FavoritesContract.Effect
import com.nikpapajohn.moviedb.ui.favorites.FavoritesContract.Intent
import com.nikpapajohn.moviedb.ui.favorites.FavoritesContract.State
import com.nikpapajohn.moviedb.ui.theme.MovieDbTheme

@Composable
fun FavoritesRoute(
    onNavigateToDetails: (Int) -> Unit,
    onMenuClick: () -> Unit,
    viewModel: FavoritesViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = rememberSnackbarController()

    // The cached snapshot renders instantly (offline-first); this quietly brings titles and
    // genres up to date whenever the screen is freshly composed — including right after a
    // system language change, which recreates the Activity but not the ViewModel.
    LaunchedEffect(Unit) { viewModel.onIntent(Intent.Refresh) }

    ObserveEffects(viewModel.effects) { effect ->
        when (effect) {
            is Effect.NavigateToDetails -> onNavigateToDetails(effect.movieId)
            is Effect.ShowMessage -> snackbar.show(effect.text)
        }
    }

    FavoritesScreen(
        state = state,
        message = snackbar.current,
        onMessageShown = snackbar::consume,
        onMenuClick = onMenuClick,
        onIntent = viewModel::onIntent,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    state: State,
    message: SnackbarRequest?,
    onMessageShown: () -> Unit,
    onMenuClick: () -> Unit,
    onIntent: (Intent) -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    SnackbarMessage(message = message, hostState = snackbarHostState, onShown = onMessageShown)

    Scaffold(
        snackbarHost = { AppSnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets.safeDrawing
            .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.favorites_title)) },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(
                            Icons.Filled.Menu,
                            contentDescription = stringResource(R.string.action_menu),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        },
    ) { padding ->
        if (state.isEmpty) {
            EmptyState(
                title = stringResource(R.string.favorites_empty_title),
                subtitle = stringResource(R.string.favorites_empty_subtitle),
                modifier = Modifier.padding(padding),
            )
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(items = state.items, key = { it.movie.id }) { item ->
                MovieCard(
                    item = item,
                    onClick = { onIntent(Intent.MovieClicked(item.movie.id)) },
                    onToggleFavorite = { onIntent(Intent.FavoriteToggled(item.movie)) },
                )
            }
        }
    }
}

private val previewFavorites = listOf(
    MovieListItem(
        movie = Movie(id = 1, title = "The Shawshank Redemption", posterPath = null, rating = 8.7, genreNames = listOf("Drama")),
        isFavorite = true,
    ),
    MovieListItem(
        movie = Movie(id = 2, title = "The Godfather", posterPath = null, rating = 8.7, genreNames = listOf("Crime", "Drama")),
        isFavorite = true,
    ),
)

@Preview(name = "With favorites", showBackground = true)
@Composable
private fun FavoritesScreenPreview() {
    MovieDbTheme {
        FavoritesScreen(
            state = State(items = previewFavorites, isLoading = false),
            message = null,
            onMessageShown = {},
            onMenuClick = {},
            onIntent = {},
        )
    }
}

@Preview(name = "Empty", showBackground = true)
@Composable
private fun FavoritesScreenEmptyPreview() {
    MovieDbTheme {
        FavoritesScreen(
            state = State(items = emptyList(), isLoading = false),
            message = null,
            onMessageShown = {},
            onMenuClick = {},
            onIntent = {},
        )
    }
}
