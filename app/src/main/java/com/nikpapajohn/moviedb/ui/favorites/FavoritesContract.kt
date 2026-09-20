package com.nikpapajohn.moviedb.ui.favorites

import androidx.compose.runtime.Immutable
import com.nikpapajohn.moviedb.core.UiText
import com.nikpapajohn.moviedb.domain.model.Movie
import com.nikpapajohn.moviedb.domain.model.MovieListItem

object FavoritesContract {

    // @Immutable: every instance is produced via copy()/map{} in reduce(); the items list is
    // never mutated in place.
    @Immutable
    data class State(
        val items: List<MovieListItem> = emptyList(),
        val isLoading: Boolean = true,
    ) {
        val isEmpty: Boolean get() = items.isEmpty() && !isLoading
    }

    sealed interface Intent {
        data class MovieClicked(val movieId: Int) : Intent
        data class FavoriteToggled(val movie: Movie) : Intent

        /** Re-syncs title/genres with the current app language. Silent: no loading state. */
        data object Refresh : Intent
    }

    sealed interface Effect {
        data class NavigateToDetails(val movieId: Int) : Effect
        data class ShowMessage(val text: UiText) : Effect
    }

    sealed interface Change {
        data class FavoritesLoaded(val movies: List<Movie>) : Change
    }

    fun reduce(state: State, change: Change): State = when (change) {
        is Change.FavoritesLoaded -> state.copy(
            items = change.movies.map { MovieListItem(movie = it, isFavorite = true) },
            isLoading = false,
        )
    }
}
