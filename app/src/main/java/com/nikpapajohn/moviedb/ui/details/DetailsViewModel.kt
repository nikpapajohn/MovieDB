package com.nikpapajohn.moviedb.ui.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nikpapajohn.moviedb.R
import com.nikpapajohn.moviedb.core.UiText
import com.nikpapajohn.moviedb.core.toAppError
import com.nikpapajohn.moviedb.domain.usecase.GetMovieDetailsUseCase
import com.nikpapajohn.moviedb.domain.usecase.ObserveIsFavoriteUseCase
import com.nikpapajohn.moviedb.domain.usecase.ToggleFavoriteUseCase
import com.nikpapajohn.moviedb.ui.details.DetailsContract.Change
import com.nikpapajohn.moviedb.ui.details.DetailsContract.Effect
import com.nikpapajohn.moviedb.ui.details.DetailsContract.Intent
import com.nikpapajohn.moviedb.ui.details.DetailsContract.State
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
class DetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getMovieDetails: GetMovieDetailsUseCase,
    private val observeIsFavorite: ObserveIsFavoriteUseCase,
    private val toggleFavorite: ToggleFavoriteUseCase,
) : ViewModel() {

    private val movieId: Int = checkNotNull(savedStateHandle.get<Int>(MOVIE_ID_KEY)) {
        "Details was opened without a movieId"
    }

    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    private val _effects = Channel<Effect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        viewModelScope.launch {
            observeIsFavorite(movieId).collect { update(Change.FavoriteUpdated(it)) }
        }
        onIntent(Intent.Load)
    }

    fun onIntent(intent: Intent) {
        when (intent) {
            Intent.Load, Intent.Retry -> load()

            Intent.BackClicked -> emit(Effect.NavigateBack)

            Intent.ShareClicked -> _state.value.details?.let { details ->
                emit(Effect.ShareText("${details.title} - ${tmdbUrl(details.id)}"))
            }

            Intent.OpenInTmdbClicked -> emit(Effect.OpenUrl(tmdbUrl(movieId)))

            Intent.ToggleFavorite -> {
                val movie = _state.value.details?.toMovie() ?: return
                viewModelScope.launch {
                    val isFavorite = toggleFavorite(movie)
                    emit(
                        Effect.ShowMessage(
                            UiText.res(
                                if (isFavorite) R.string.message_added_to_favorites
                                else R.string.message_removed_from_favorites,
                            ),
                        ),
                    )
                }
            }
        }
    }

    private fun load() {
        viewModelScope.launch {
            update(Change.Loading)
            getMovieDetails(movieId)
                .onSuccess { update(Change.Loaded(it)) }
                .onFailure { update(Change.Failed(it.toAppError().toUiText())) }
        }
    }

    private fun update(change: Change) {
        _state.update { DetailsContract.reduce(it, change) }
    }

    private fun emit(effect: Effect) {
        viewModelScope.launch { _effects.send(effect) }
    }

    private fun tmdbUrl(id: Int) = "https://www.themoviedb.org/movie/$id"

    private companion object {
        /** Field name of Destination.Details: how type-safe routes store their arguments. */
        const val MOVIE_ID_KEY = "movieId"
    }
}
