package com.nikpapajohn.moviedb.ui.favorites

import com.nikpapajohn.moviedb.domain.model.MovieListItem
import com.nikpapajohn.moviedb.ui.favorites.FavoritesContract.Change
import com.nikpapajohn.moviedb.ui.favorites.FavoritesContract.State

/** Pure state transitions, in their own file like every other feature's reducer. */
object FavoritesReducer {

    fun reduce(state: State, change: Change): State = when (change) {
        is Change.FavoritesLoaded -> state.copy(
            items = change.movies.map { MovieListItem(movie = it, isFavorite = true) },
            isLoading = false,
        )
    }
}
