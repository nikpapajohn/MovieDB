package com.nikpapajohn.moviedb.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nikpapajohn.moviedb.R
import com.nikpapajohn.moviedb.core.UiText
import com.nikpapajohn.moviedb.domain.usecase.ObserveFavoritesUseCase
import com.nikpapajohn.moviedb.domain.usecase.ToggleFavoriteUseCase
import com.nikpapajohn.moviedb.ui.favorites.FavoritesContract.Change
import com.nikpapajohn.moviedb.ui.favorites.FavoritesContract.Effect
import com.nikpapajohn.moviedb.ui.favorites.FavoritesContract.Intent
import com.nikpapajohn.moviedb.ui.favorites.FavoritesContract.State
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

            is Intent.FavoriteToggled -> viewModelScope.launch {
                toggleFavorite(intent.movie)
                emit(Effect.ShowMessage(UiText.res(R.string.message_removed_from_favorites)))
            }
        }
    }

    private fun update(change: Change) {
        _state.update { FavoritesContract.reduce(it, change) }
    }

    private fun emit(effect: Effect) {
        viewModelScope.launch { _effects.send(effect) }
    }
}
