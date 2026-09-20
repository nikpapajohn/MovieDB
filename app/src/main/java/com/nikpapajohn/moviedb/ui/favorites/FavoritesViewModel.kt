package com.nikpapajohn.moviedb.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nikpapajohn.moviedb.R
import com.nikpapajohn.moviedb.core.UiText
import com.nikpapajohn.moviedb.core.safeCall
import com.nikpapajohn.moviedb.core.toAppError
import com.nikpapajohn.moviedb.domain.usecase.ObserveFavoritesUseCase
import com.nikpapajohn.moviedb.domain.usecase.RefreshFavoritesUseCase
import com.nikpapajohn.moviedb.domain.usecase.ToggleFavoriteUseCase
import com.nikpapajohn.moviedb.ui.favorites.FavoritesContract.Change
import com.nikpapajohn.moviedb.ui.favorites.FavoritesContract.Effect
import com.nikpapajohn.moviedb.ui.favorites.FavoritesContract.Intent
import com.nikpapajohn.moviedb.ui.favorites.FavoritesContract.State
import com.nikpapajohn.moviedb.ui.toUiText
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    observeFavorites: ObserveFavoritesUseCase,
    private val toggleFavorite: ToggleFavoriteUseCase,
    private val refreshFavorites: RefreshFavoritesUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    private val _effects = Channel<Effect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        viewModelScope.launch {
            observeFavorites().collect { movies -> update(Change.FavoritesLoaded(movies)) }
        }
    }

    fun onIntent(intent: Intent) {
        when (intent) {
            is Intent.MovieClicked -> emit(Effect.NavigateToDetails(intent.movieId))

            // The message follows what the toggle actually did rather than assuming a
            // removal: every row here starts out a favorite, but that is a property of the
            // list at one moment, not a guarantee about the write that just happened.
            is Intent.FavoriteToggled -> viewModelScope.launch {
                safeCall { toggleFavorite(intent.movie) }
                    .onSuccess { isFavorite ->
                        emit(
                            Effect.ShowMessage(
                                UiText.res(
                                    if (isFavorite) R.string.message_added_to_favorites
                                    else R.string.message_removed_from_favorites,
                                ),
                            ),
                        )
                    }
                    .onFailure { emit(Effect.ShowMessage(it.toAppError().toUiText())) }
            }

            // Best-effort and silent: the cached snapshot is already on screen, this just
            // brings title/genres up to date with the current app language when possible.
            // safeCall, not runCatching, so leaving the screen cancels it instead of being
            // swallowed as a failure.
            Intent.Refresh -> viewModelScope.launch { safeCall { refreshFavorites() } }
        }
    }

    private fun update(change: Change) {
        _state.update { FavoritesReducer.reduce(it, change) }
    }

    private fun emit(effect: Effect) {
        viewModelScope.launch { _effects.send(effect) }
    }
}
