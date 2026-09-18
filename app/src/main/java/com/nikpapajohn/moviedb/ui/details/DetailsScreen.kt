package com.nikpapajohn.moviedb.ui.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nikpapajohn.moviedb.R
import com.nikpapajohn.moviedb.core.PosterSize
import com.nikpapajohn.moviedb.core.UiText
import com.nikpapajohn.moviedb.domain.model.MovieDetails
import com.nikpapajohn.moviedb.ui.ObserveEffects
import com.nikpapajohn.moviedb.ui.components.ErrorState
import com.nikpapajohn.moviedb.ui.components.LoadingState
import com.nikpapajohn.moviedb.ui.components.PosterImage
import com.nikpapajohn.moviedb.ui.components.RatingRow
import com.nikpapajohn.moviedb.ui.components.SnackbarMessage
import com.nikpapajohn.moviedb.ui.details.DetailsContract.Effect
import com.nikpapajohn.moviedb.ui.details.DetailsContract.Intent
import com.nikpapajohn.moviedb.ui.details.DetailsContract.State

const val FAVORITE_BUTTON_TAG = "details_favorite_button"

@Composable
fun DetailsRoute(
    onNavigateBack: () -> Unit,
    viewModel: DetailsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var message by remember { mutableStateOf<UiText?>(null) }

    ObserveEffects(viewModel.effects) { effect ->
        when (effect) {
            Effect.NavigateBack -> onNavigateBack()
            is Effect.ShowMessage -> message = effect.text
        }
    }

    DetailsScreen(
        state = state,
        message = message,
        onMessageShown = { message = null },
        onIntent = viewModel::onIntent,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    state: State,
    message: UiText?,
    onMessageShown: () -> Unit,
    onIntent: (Intent) -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    SnackbarMessage(message = message, hostState = snackbarHostState, onShown = onMessageShown)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets.safeDrawing
            .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.details_title)) },
                navigationIcon = {
                    IconButton(onClick = { onIntent(Intent.BackClicked) }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
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
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            when {
                state.isLoading && state.details == null -> LoadingState()

                state.error != null && state.details == null -> ErrorState(
                    message = state.error,
                    onRetry = { onIntent(Intent.Retry) },
                )

                state.details != null -> DetailsContent(
                    details = state.details,
                    isFavorite = state.isFavorite,
                    onToggleFavorite = { onIntent(Intent.ToggleFavorite) },
                )
            }
        }
    }
}

@Composable
private fun DetailsContent(
    details: MovieDetails,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            PosterImage(
                posterPath = details.posterPath,
                size = PosterSize.DETAILS,
                contentDescription = stringResource(R.string.cd_poster, details.title),
                modifier = Modifier
                    .width(140.dp)
                    .height(210.dp),
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = details.title, style = MaterialTheme.typography.titleLarge)
                details.tagline?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                RatingRow(rating = details.rating, voteCount = details.voteCount, starSize = 22)
                GenreChipRow(details)
            }
        }

        FavoriteButton(isFavorite = isFavorite, onToggleFavorite = onToggleFavorite)

        FactsCard(details)

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(R.string.details_overview),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = details.overview.ifBlank { stringResource(R.string.details_no_overview) },
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun GenreChipRow(details: MovieDetails) {
    if (details.genres.isEmpty()) return
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        details.genres.take(3).forEach { genre ->
            AssistChip(
                onClick = {},
                enabled = false,
                label = { Text(genre.name) },
                colors = AssistChipDefaults.assistChipColors(
                    disabledContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    disabledLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            )
        }
    }
}

@Composable
private fun FavoriteButton(isFavorite: Boolean, onToggleFavorite: () -> Unit) {
    val label = stringResource(
        if (isFavorite) R.string.details_added_favorite else R.string.details_add_favorite,
    )
    if (isFavorite) {
        Button(
            onClick = onToggleFavorite,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(FAVORITE_BUTTON_TAG),
        ) {
            Icon(Icons.Filled.Favorite, contentDescription = null)
            Text(text = label, modifier = Modifier.padding(start = 8.dp))
            Icon(
                Icons.Filled.Check,
                contentDescription = null,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    } else {
        OutlinedButton(
            onClick = onToggleFavorite,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(FAVORITE_BUTTON_TAG),
        ) {
            Icon(Icons.Outlined.FavoriteBorder, contentDescription = null)
            Text(text = label, modifier = Modifier.padding(start = 8.dp))
        }
    }
}

@Composable
private fun FactsCard(details: MovieDetails) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Fact(
                icon = { Icon(Icons.Filled.CalendarMonth, contentDescription = null) },
                label = stringResource(R.string.details_release_date),
                value = details.releaseYear ?: "—",
            )
            Fact(
                icon = { Icon(Icons.Filled.Schedule, contentDescription = null) },
                label = stringResource(R.string.details_runtime),
                value = details.runtimeMinutes
                    ?.let { stringResource(R.string.details_runtime_minutes, it) }
                    ?: "—",
            )
        }
    }
}

@Composable
private fun Fact(icon: @Composable () -> Unit, label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            icon()
            Text(text = label, style = MaterialTheme.typography.bodySmall)
        }
        Text(text = value, style = MaterialTheme.typography.titleMedium)
    }
}
