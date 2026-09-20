package com.nikpapajohn.moviedb.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nikpapajohn.moviedb.R
import com.nikpapajohn.moviedb.core.UiText
import com.nikpapajohn.moviedb.ui.ObserveEffects
import com.nikpapajohn.moviedb.ui.components.AppSnackbarHost
import com.nikpapajohn.moviedb.ui.components.SnackbarMessage
import com.nikpapajohn.moviedb.ui.profile.ProfileContract.Effect
import com.nikpapajohn.moviedb.ui.profile.ProfileContract.Intent
import com.nikpapajohn.moviedb.ui.profile.ProfileContract.State
import com.nikpapajohn.moviedb.ui.theme.MovieDbTheme

@Composable
fun ProfileRoute(
    onMenuClick: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var message by remember { mutableStateOf<UiText?>(null) }

    ObserveEffects(viewModel.effects) { effect ->
        when (effect) {
            is Effect.ShowMessage -> message = effect.text
        }
    }

    ProfileScreen(
        state = state,
        message = message,
        onMessageShown = { message = null },
        onMenuClick = onMenuClick,
        onIntent = viewModel::onIntent,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    state: State,
    message: UiText?,
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
                title = { Text(stringResource(R.string.profile_title)) },
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
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = stringResource(R.string.profile_storage_title),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = stringResource(R.string.profile_favorites_count, state.favoritesCount),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            OutlinedButton(
                onClick = { onIntent(Intent.ClearFavoritesClicked) },
                enabled = state.favoritesCount > 0,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.profile_clear_favorites))
            }

        }
    }
}

@Preview(name = "With favorites", showBackground = true)
@Composable
private fun ProfileScreenPreview() {
    MovieDbTheme {
        ProfileScreen(
            state = State(favoritesCount = 12),
            message = null,
            onMessageShown = {},
            onMenuClick = {},
            onIntent = {},
        )
    }
}

@Preview(name = "No favorites", showBackground = true)
@Composable
private fun ProfileScreenEmptyPreview() {
    MovieDbTheme {
        ProfileScreen(
            state = State(favoritesCount = 0),
            message = null,
            onMessageShown = {},
            onMenuClick = {},
            onIntent = {},
        )
    }
}

