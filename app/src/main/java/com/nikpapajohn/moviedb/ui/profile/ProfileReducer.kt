package com.nikpapajohn.moviedb.ui.profile

import com.nikpapajohn.moviedb.ui.profile.ProfileContract.Change
import com.nikpapajohn.moviedb.ui.profile.ProfileContract.State

/** Pure state transitions, in their own file like every other feature's reducer. */
object ProfileReducer {

    fun reduce(state: State, change: Change): State = when (change) {
        is Change.CountUpdated -> state.copy(favoritesCount = change.count)
    }
}
