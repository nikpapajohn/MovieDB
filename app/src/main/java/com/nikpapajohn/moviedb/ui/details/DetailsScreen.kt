package com.nikpapajohn.moviedb.ui.details

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent as AndroidIntent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nikpapajohn.moviedb.R
import com.nikpapajohn.moviedb.core.PosterSize
import com.nikpapajohn.moviedb.core.UiText
import com.nikpapajohn.moviedb.domain.model.Genre
import com.nikpapajohn.moviedb.domain.model.MovieDetails
import com.nikpapajohn.moviedb.ui.ObserveEffects
import com.nikpapajohn.moviedb.ui.components.AppSnackbarHost
import com.nikpapajohn.moviedb.ui.components.ErrorState
import com.nikpapajohn.moviedb.ui.components.FavoriteButton
import com.nikpapajohn.moviedb.ui.components.GenreChip
import com.nikpapajohn.moviedb.ui.components.LoadingState
import com.nikpapajohn.moviedb.ui.components.PosterImage
import com.nikpapajohn.moviedb.ui.components.RatingRow
import com.nikpapajohn.moviedb.ui.components.SnackbarMessage
import com.nikpapajohn.moviedb.ui.components.SnackbarRequest
import com.nikpapajohn.moviedb.ui.components.rememberSnackbarController
import com.nikpapajohn.moviedb.ui.details.DetailsContract.Effect
import com.nikpapajohn.moviedb.ui.details.DetailsContract.Intent
import com.nikpapajohn.moviedb.ui.details.DetailsContract.State
import com.nikpapajohn.moviedb.ui.theme.MovieDbTheme

@Composable
fun DetailsRoute(onNavigateBack: () -> Unit, viewModel: DetailsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = rememberSnackbarController()
    val context = LocalContext.current

    ObserveEffects(viewModel.effects) { effect ->
        when (effect) {
            Effect.NavigateBack -> onNavigateBack()
            is Effect.ShowMessage -> snackbar.show(effect.text)
            is Effect.ShareText -> context.shareText(effect.text)
            is Effect.OpenUrl -> context.openUrl(effect.url)
        }
    }

    DetailsScreen(
        state = state,
        message = snackbar.current,
        onMessageShown = snackbar::consume,
        onIntent = viewModel::onIntent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    state: State,
    message: SnackbarRequest?,
    onMessageShown: () -> Unit,
    onIntent: (Intent) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var menuExpanded by remember { mutableStateOf(false) }
    SnackbarMessage(message = message, hostState = snackbarHostState, onShown = onMessageShown)

    Scaffold(
        snackbarHost = { AppSnackbarHost(snackbarHostState) },
        // Full-screen destination: there is no app bottom bar under it, so this screen owns
        // the bottom inset too. Without it the snackbar lands under the navigation buttons.
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.details_title),
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onIntent(Intent.BackClicked) }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { menuExpanded = true },
                        enabled = state.details != null
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = stringResource(R.string.action_more)
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.action_share)) },
                            onClick = {
                                menuExpanded = false
                                onIntent(Intent.ShareClicked)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.action_open_tmdb)) },
                            onClick = {
                                menuExpanded = false
                                onIntent(Intent.OpenInTmdbClicked)
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            when {
                state.isLoading && state.details == null -> LoadingState()

                state.error != null && state.details == null -> ErrorState(
                    message = state.error,
                    onRetry = { onIntent(Intent.Retry) }
                )

                state.details != null -> DetailsContent(
                    details = state.details,
                    isFavorite = state.isFavorite,
                    onToggleFavorite = { onIntent(Intent.ToggleFavorite) }
                )
            }
        }
    }
}

@Composable
private fun DetailsContent(
    details: MovieDetails,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit
) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            // Order matters: widthIn has to come before fillMaxWidth. The other way round,
            // fillMaxWidth pins the minimum width to the parent's and the cap is ignored.
            // Landscape and tablets: the column stops growing instead of stretching a poster
            // block across 1000dp of screen.
            .widthIn(max = 640.dp)
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            PosterImage(
                posterPath = details.posterPath,
                size = PosterSize.DETAILS,
                contentDescription = stringResource(R.string.cd_poster, details.title),
                modifier = Modifier
                    .width(150.dp)
                    .height(260.dp)
            )
            Column(
                // Matches the poster's height so the row's content spreads across it instead
                // of clumping at the top: title lower, rating near the poster's middle, chips
                // and the favorite button carried down toward its bottom edge, like the mock.
                // Taller than the strict minimum so a wrapped title/genre row or a two-line
                // favorite button label doesn't push the button past the bottom edge and
                // clip it.
                modifier = Modifier
                    .weight(1f)
                    .height(260.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // An empty first item so SpaceBetween's even gaps include one above the
                // title too — otherwise the title would sit flush against the top edge.
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = details.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                RatingRow(
                    rating = details.rating,
                    voteCount = details.voteCount,
                    starSize = 18,
                    ratingStyle = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    voteCountStyle = MaterialTheme.typography.labelSmall
                )
                GenreChipRow(details)
                FavoriteButton(isFavorite = isFavorite, onToggleFavorite = onToggleFavorite)
            }
        }

        // Extra breathing room above and below, so the card is as separated from the poster
        // block as it is from the overview.
        FactsCard(details, modifier = Modifier.padding(vertical = 8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.details_overview),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                details.tagline?.let { tagline ->
                    Text(
                        text = tagline,
                        style = MaterialTheme.typography.bodyMedium,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = details.overview.ifBlank {
                        stringResource(R.string.details_no_overview)
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

/**
 * FlowRow keeps the genre chips on one line and wraps to a second only when they do not
 * fit; the chip itself is the shared GenreChip component.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GenreChipRow(details: MovieDetails) {
    if (details.genres.isEmpty()) return
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        details.genres.take(3).forEach { genre ->
            GenreChip(text = genre.name)
        }
    }
}

@Composable
private fun FactsCard(details: MovieDetails, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Fact(
                icon = { Icon(Icons.Filled.CalendarToday, contentDescription = null) },
                label = stringResource(R.string.details_release_date),
                value = details.releaseYear ?: "—",
                modifier = Modifier.weight(1f)
            )
            Fact(
                icon = { Icon(Icons.Filled.Schedule, contentDescription = null) },
                label = stringResource(R.string.details_runtime),
                value = details.runtimeMinutes
                    ?.let { pluralStringResource(R.plurals.details_runtime_minutes, it, it) }
                    ?: "—",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun Fact(
    icon: @Composable () -> Unit,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        icon()
        // Label and value share the column to the right of the icon, so the value lines up
        // under its own title rather than under the icon.
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = label, style = MaterialTheme.typography.bodySmall)
            Text(text = value, style = MaterialTheme.typography.titleMedium)
        }
    }
}

/**
 * The ViewModel decides what to share; only the UI layer touches Android intents.
 * Both calls are guarded: a device with no browser or share target must not crash the app.
 */
private fun Context.shareText(text: String) {
    val send = AndroidIntent(AndroidIntent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(AndroidIntent.EXTRA_TEXT, text)
    }
    runCatching { startActivity(AndroidIntent.createChooser(send, null)) }
}

private fun Context.openUrl(url: String) {
    try {
        startActivity(AndroidIntent(AndroidIntent.ACTION_VIEW, url.toUri()))
    } catch (e: ActivityNotFoundException) {
        // No browser installed; nothing useful to do beyond ignoring the tap.
    }
}

private val previewDetails = MovieDetails(
    id = 278,
    title = "The Shawshank Redemption",
    tagline = "Fear can hold you prisoner. Hope can set you free.",
    overview = "Two imprisoned men bond over a number of years, finding solace and " +
        "eventual redemption through acts of common decency.",
    posterPath = null,
    rating = 8.7,
    voteCount = 26000,
    releaseDate = "1994-09-23",
    runtimeMinutes = 142,
    genres = listOf(Genre(id = 18, name = "Drama"), Genre(id = 80, name = "Crime"))
)

@Preview(name = "Loaded", showBackground = true)
@Composable
private fun DetailsScreenLoadedPreview() {
    MovieDbTheme {
        DetailsScreen(
            state = State(isLoading = false, details = previewDetails, isFavorite = true),
            message = null,
            onMessageShown = {},
            onIntent = {}
        )
    }
}

@Preview(name = "Loading", showBackground = true)
@Composable
private fun DetailsScreenLoadingPreview() {
    MovieDbTheme {
        DetailsScreen(
            state = State(isLoading = true),
            message = null,
            onMessageShown = {},
            onIntent = {}
        )
    }
}

@Preview(name = "Error", showBackground = true)
@Composable
private fun DetailsScreenErrorPreview() {
    MovieDbTheme {
        DetailsScreen(
            state = State(isLoading = false, error = UiText.Dynamic("Couldn't load this movie")),
            message = null,
            onMessageShown = {},
            onIntent = {}
        )
    }
}
