package com.nikpapajohn.moviedb.ui.profile

import androidx.compose.runtime.Immutable
import com.nikpapajohn.moviedb.core.UiText

object ProfileContract {

    @Immutable
    data class State(val favoritesCount: Int = 0)

    sealed interface Intent {
        data object ClearFavoritesClicked : Intent
    }

    sealed interface Effect {
        data class ShowMessage(val text: UiText) : Effect
    }

    sealed interface Change {
        data class CountUpdated(val count: Int) : Change
    }

    fun reduce(state: State, change: Change): State = when (change) {
        is Change.CountUpdated -> state.copy(favoritesCount = change.count)
    }
}
