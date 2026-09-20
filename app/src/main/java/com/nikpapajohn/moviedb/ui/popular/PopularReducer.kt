package com.nikpapajohn.moviedb.ui.popular

import com.nikpapajohn.moviedb.domain.model.MovieListItem
import com.nikpapajohn.moviedb.ui.popular.PopularContract.Change
import com.nikpapajohn.moviedb.ui.popular.PopularContract.State

object PopularReducer {

    fun reduce(state: State, change: Change): State = when (change) {
        is Change.QueryUpdated -> state.copy(query = change.value)

        Change.SearchToggled -> {
            val visible = !state.isSearchVisible
            state.copy(
                isSearchVisible = visible,
                // Closing the search box clears the query, which triggers a reload of popular.
                query = if (visible) state.query else ""
            )
        }

        Change.FirstPageLoading -> state.copy(
            isLoading = true,
            isLoadingMore = false,
            error = null,
            appendError = null
        )

        Change.NextPageLoading -> state.copy(isLoadingMore = true, appendError = null)

        is Change.PageLoaded -> {
            val newItems = change.movies.map { movie ->
                MovieListItem(movie = movie, isFavorite = state.favoriteIds.contains(movie.id))
            }
            val merged = if (change.replace) {
                newItems
            } else {
                // TMDB can repeat a title across pages while the ranking shifts under us.
                val known = state.items.map { it.movie.id }.toSet()
                state.items + newItems.filterNot { known.contains(it.movie.id) }
            }
            state.copy(
                items = merged,
                page = change.page,
                endReached = change.endReached,
                isLoading = false,
                isLoadingMore = false,
                error = null,
                appendError = null
            )
        }

        is Change.LoadFailed -> if (change.isAppend) {
            state.copy(isLoadingMore = false, appendError = change.error)
        } else {
            state.copy(isLoading = false, items = emptyList(), error = change.error)
        }

        is Change.FavoritesUpdated -> state.copy(
            favoriteIds = change.ids,
            items = state.items.map { item ->
                item.copy(isFavorite = change.ids.contains(item.movie.id))
            }
        )
    }
}
