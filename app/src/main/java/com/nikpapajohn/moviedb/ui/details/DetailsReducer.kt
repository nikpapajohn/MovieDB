package com.nikpapajohn.moviedb.ui.details

import com.nikpapajohn.moviedb.ui.details.DetailsContract.Change
import com.nikpapajohn.moviedb.ui.details.DetailsContract.State

/** Pure state transitions, in their own file like every other feature's reducer. */
object DetailsReducer {

    fun reduce(state: State, change: Change): State = when (change) {
        Change.Loading -> state.copy(isLoading = true, error = null)
        is Change.Loaded -> state.copy(isLoading = false, details = change.details, error = null)
        is Change.Failed -> state.copy(isLoading = false, error = change.error)
        is Change.FavoriteUpdated -> state.copy(isFavorite = change.isFavorite)
    }
}
