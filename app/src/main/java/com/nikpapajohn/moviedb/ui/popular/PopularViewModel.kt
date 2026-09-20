package com.nikpapajohn.moviedb.ui.popular

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nikpapajohn.moviedb.core.toAppError
import com.nikpapajohn.moviedb.domain.model.MoviePage
import com.nikpapajohn.moviedb.domain.usecase.GetPopularMoviesUseCase
import com.nikpapajohn.moviedb.domain.usecase.ObserveFavoriteIdsUseCase
import com.nikpapajohn.moviedb.domain.usecase.SearchMoviesUseCase
import com.nikpapajohn.moviedb.domain.usecase.ToggleFavoriteUseCase
import com.nikpapajohn.moviedb.ui.popular.PopularContract.Change
import com.nikpapajohn.moviedb.ui.popular.PopularContract.Effect
import com.nikpapajohn.moviedb.ui.popular.PopularContract.Intent
import com.nikpapajohn.moviedb.ui.popular.PopularContract.State
import com.nikpapajohn.moviedb.ui.toUiText
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class PopularViewModel @Inject constructor(
    private val getPopularMovies: GetPopularMoviesUseCase,
    private val searchMovies: SearchMoviesUseCase,
    private val observeFavoriteIds: ObserveFavoriteIdsUseCase,
    private val toggleFavorite: ToggleFavoriteUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    /** One-off events. A Channel, not a StateFlow: they must fire once, not be replayed. */
    private val _effects = Channel<Effect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var loadJob: Job? = null

    init {
        viewModelScope.launch {
            observeFavoriteIds().collect { ids -> update(Change.FavoritesUpdated(ids)) }
        }
        onIntent(Intent.Load)
    }

    /** The single entry point from the UI. */
    fun onIntent(intent: Intent) {
        when (intent) {
            Intent.Load, Intent.Retry -> loadFirstPage(debounce = false)

            Intent.LoadNextPage -> loadNextPage()

            is Intent.QueryChanged -> {
                update(Change.QueryUpdated(intent.value))
                loadFirstPage(debounce = true)
            }

            Intent.SearchToggled -> {
                val wasVisible = _state.value.isSearchVisible
                update(Change.SearchToggled)
                // Leaving search goes back to the popular list.
                if (wasVisible) loadFirstPage(debounce = false)
            }

            is Intent.MovieClicked -> emit(Effect.NavigateToDetails(intent.movieId))

            Intent.FavoritesClicked -> emit(Effect.NavigateToFavorites)

            is Intent.FavoriteToggled -> viewModelScope.launch {
                toggleFavorite(intent.movie)
            }
        }
    }

    private fun loadFirstPage(debounce: Boolean) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            if (debounce) delay(SEARCH_DEBOUNCE_MS)
            update(Change.FirstPageLoading)
            load(page = MoviePage.FIRST_PAGE, replace = true)
        }
    }

    private fun loadNextPage() {
        val current = _state.value
        // canLoadMore is the auto-scroll guard; an explicit retry after an append error
        // must get through, so the condition is spelled out here instead.
        val busyOrDone = current.isLoading || current.isLoadingMore || current.endReached
        if (busyOrDone || current.items.isEmpty()) return
        loadJob = viewModelScope.launch {
            update(Change.NextPageLoading)
            load(page = current.page + 1, replace = false)
        }
    }

    private suspend fun load(page: Int, replace: Boolean) {
        val query = _state.value.query
        val result = if (query.isBlank()) {
            getPopularMovies(page)
        } else {
            searchMovies(query, page)
        }
        result
            .onSuccess { moviePage ->
                update(
                    Change.PageLoaded(
                        movies = moviePage.movies,
                        page = moviePage.page,
                        endReached = !moviePage.hasMorePages,
                        replace = replace,
                    ),
                )
            }
            .onFailure { throwable ->
                update(
                    Change.LoadFailed(
                        error = throwable.toAppError().toUiText(),
                        isAppend = !replace,
                    ),
                )
            }
    }

    private fun update(change: Change) {
        _state.update { current -> PopularReducer.reduce(current, change) }
    }

    private fun emit(effect: Effect) {
        viewModelScope.launch { _effects.send(effect) }
    }

    private companion object {
        const val SEARCH_DEBOUNCE_MS = 500L
    }
}
