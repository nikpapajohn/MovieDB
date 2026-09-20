package com.nikpapajohn.moviedb.ui.profile

import com.nikpapajohn.moviedb.ui.profile.ProfileContract.Change
import com.nikpapajohn.moviedb.ui.profile.ProfileContract.State

object ProfileReducer {

    fun reduce(state: State, change: Change): State = when (change) {
        is Change.CountUpdated -> state.copy(favoritesCount = change.count)
    }
}
