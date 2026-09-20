package com.nikpapajohn.moviedb.ui.popular

import android.content.res.Configuration

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nikpapajohn.moviedb.R
import com.nikpapajohn.moviedb.core.UiText
import com.nikpapajohn.moviedb.domain.model.Movie
import com.nikpapajohn.moviedb.domain.model.MovieListItem
import com.nikpapajohn.moviedb.ui.ObserveEffects
import com.nikpapajohn.moviedb.ui.components.AppSnackbarHost
import com.nikpapajohn.moviedb.ui.components.ErrorState
import com.nikpapajohn.moviedb.ui.components.LoadingState
import com.nikpapajohn.moviedb.ui.components.MovieCard
import com.nikpapajohn.moviedb.ui.components.SnackbarMessage
import com.nikpapajohn.moviedb.ui.popular.PopularContract.Effect
import com.nikpapajohn.moviedb.ui.popular.PopularContract.Intent
import com.nikpapajohn.moviedb.ui.popular.PopularContract.State
import com.nikpapajohn.moviedb.ui.theme.MovieDbTheme

@Composable
fun PopularRoute(
    onNavigateToDetails: (Int) -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    onMenuClick: () -> Unit,
    viewModel: PopularViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var message by remember { mutableStateOf<UiText?>(null) }

    ObserveEffects(viewModel.effects) { effect ->
        when (effect) {
            is Effect.NavigateToDetails -> onNavigateToDetails(effect.movieId)
            Effect.NavigateToFavorites -> onNavigateToFavorites()
            is Effect.ShowMessage -> message = effect.text
        }
    }

    PopularScreen(
        state = state,
        message = message,
        onMessageShown = { message = null },
        onMenuClick = onMenuClick,
        onAboutClick = onNavigateToAbout,
        onIntent = viewModel::onIntent,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PopularScreen(
    state: State,
    message: UiText?,
    onMessageShown: () -> Unit,
    onMenuClick: () -> Unit,
    onAboutClick: () -> Unit,
    onIntent: (Intent) -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var menuExpanded by remember { mutableStateOf(false) }
    val isLandscape =
        LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val searchFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    var isSearchFieldFocused by remember { mutableStateOf(false) }
    SnackbarMessage(message = message, hostState = snackbarHostState, onShown = onMessageShown)

    // Tapping the search icon reveals the field; it should also grab focus and raise the
    // keyboard right away instead of leaving the user to tap it a second time.
    LaunchedEffect(state.isSearchVisible) {
        if (state.isSearchVisible) {
            searchFocusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    // The keyboard's own check/search action hides the keyboard AND clears focus (handled
    // where the field is declared). The Android back button only hides the keyboard — it
    // never calls onSearch — which left the field looking "active" with no visible keyboard.
    // isImeVisible catches both paths, so back gets the same clear-focus behaviour for free.
    val imeVisible = WindowInsets.isImeVisible
    LaunchedEffect(imeVisible) {
        if (!imeVisible && isSearchFieldFocused) {
            focusManager.clearFocus()
        }
    }

    Scaffold(
        snackbarHost = { AppSnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets.safeDrawing
            .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.home_title)) },
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
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                actions = {
                    IconButton(onClick = { onIntent(Intent.SearchToggled) }) {
                        Icon(
                            imageVector = if (state.isSearchVisible) Icons.Filled.Close else Icons.Filled.Search,
                            contentDescription = stringResource(
                                if (state.isSearchVisible) R.string.action_close_search
                                else R.string.action_search,
                            ),
                        )
                    }
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = stringResource(R.string.action_more),
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.action_refresh)) },
                            onClick = {
                                menuExpanded = false
                                onIntent(Intent.Retry)
                            },
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.nav_about)) },
                            onClick = {
                                menuExpanded = false
                                onAboutClick()
                            },
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            // The mockup's FAB: a circle in the brand teal with a white filled heart,
            // not the Material default (rounded square in the primary container tone).
            FloatingActionButton(
                onClick = { onIntent(Intent.FavoritesClicked) },
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                // Landscape pushes it against the navigation bar on the right edge, so it is
                // pulled back inwards; portrait keeps the standard 16dp margin.
                modifier = Modifier.padding(end = if (isLandscape) 40.dp else 0.dp),
            ) {
                Icon(
                    Icons.Filled.Favorite,
                    contentDescription = stringResource(R.string.action_open_favorites),
                )
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            if (state.isSearchVisible) {
                OutlinedTextField(
                    value = state.query,
                    onValueChange = { onIntent(Intent.QueryChanged(it)) },
                    singleLine = true,
                    placeholder = { Text(stringResource(R.string.search_hint)) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        // The keyboard's own check/search action: dismiss the keyboard and
                        // drop focus, rather than leaving the field looking still "active".
                        onSearch = {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                        },
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .focusRequester(searchFocusRequester)
                        .onFocusChanged { isSearchFieldFocused = it.isFocused },
                )
            } else if (!isLandscape) {
                // In landscape the welcome block would eat most of the visible list.
                WelcomeHeader()
            }

            Text(
                text = stringResource(
                    if (state.query.isBlank()) R.string.home_section_popular
                    else R.string.home_section_results,
                ),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )

            when {
                state.isLoading -> LoadingState()

                state.error != null -> ErrorState(
                    message = state.error,
                    onRetry = { onIntent(Intent.Retry) },
                )

                state.isEmpty -> Text(
                    text = stringResource(R.string.error_empty_results),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp),
                )

                else -> MovieList(state = state, onIntent = onIntent)
            }
        }
    }
}

@Composable
private fun WelcomeHeader() {
    Surface(color = MaterialTheme.colorScheme.primary) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = stringResource(R.string.home_welcome_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary,
            )
            Text(
                text = stringResource(R.string.home_welcome_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
            )
        }
    }
}

@Composable
private fun MovieList(
    state: State,
    onIntent: (Intent) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        // Bottom room for the FAB, so the last card is never trapped under it.
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        itemsIndexed(items = state.items, key = { _, item -> item.movie.id }) { index, item ->
            // Reaching the last row asks for the next page: this is the whole of our pagination.
            if (index == state.items.lastIndex && state.canLoadMore) {
                LaunchedEffect(state.page, state.items.size) {
                    onIntent(Intent.LoadNextPage)
                }
            }
            MovieCard(
                item = item,
                onClick = { onIntent(Intent.MovieClicked(item.movie.id)) },
                onToggleFavorite = { onIntent(Intent.FavoriteToggled(item.movie)) },
            )
        }

        if (state.isLoadingMore) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        state.appendError?.let { error ->
            item {
                ErrorState(message = error, onRetry = { onIntent(Intent.LoadNextPage) })
            }
        }
    }
}

private val previewMovies = listOf(
    MovieListItem(
        movie = Movie(id = 1, title = "The Shawshank Redemption", posterPath = null, rating = 8.7, genreNames = listOf("Drama")),
        isFavorite = true,
    ),
    MovieListItem(
        movie = Movie(id = 2, title = "The Godfather", posterPath = null, rating = 8.7, genreNames = listOf("Crime", "Drama")),
        isFavorite = false,
    ),
    MovieListItem(
        movie = Movie(id = 3, title = "The Dark Knight", posterPath = null, rating = 8.5, genreNames = listOf("Action")),
        isFavorite = false,
    ),
)

@Preview(name = "Popular list", showBackground = true)
@Composable
private fun PopularScreenPreview() {
    MovieDbTheme {
        PopularScreen(
            state = State(items = previewMovies, isLoading = false),
            message = null,
            onMessageShown = {},
            onMenuClick = {},
            onAboutClick = {},
            onIntent = {},
        )
    }
}

@Preview(name = "Search", showBackground = true)
@Composable
private fun PopularScreenSearchPreview() {
    MovieDbTheme {
        PopularScreen(
            state = State(items = previewMovies, isLoading = false, isSearchVisible = true, query = "dark"),
            message = null,
            onMessageShown = {},
            onMenuClick = {},
            onAboutClick = {},
            onIntent = {},
        )
    }
}

@Preview(name = "Loading", showBackground = true)
@Composable
private fun PopularScreenLoadingPreview() {
    MovieDbTheme {
        PopularScreen(
            state = State(isLoading = true),
            message = null,
            onMessageShown = {},
            onMenuClick = {},
            onAboutClick = {},
            onIntent = {},
        )
    }
}

@Preview(name = "Error", showBackground = true)
@Composable
private fun PopularScreenErrorPreview() {
    MovieDbTheme {
        PopularScreen(
            state = State(isLoading = false, error = UiText.Dynamic("Couldn't load movies")),
            message = null,
            onMessageShown = {},
            onMenuClick = {},
            onAboutClick = {},
            onIntent = {},
        )
    }
}
