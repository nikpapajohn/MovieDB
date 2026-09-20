package com.nikpapajohn.moviedb.ui.details

import androidx.compose.runtime.Immutable
import com.nikpapajohn.moviedb.core.UiText
import com.nikpapajohn.moviedb.domain.model.MovieDetails

object DetailsContract {

    @Immutable
    data class State(
        val isLoading: Boolean = true,
        val details: MovieDetails? = null,
        val isFavorite: Boolean = false,
        val error: UiText? = null
    )

    sealed interface Intent {
        data object Load : Intent
        data object Retry : Intent
        data object ToggleFavorite : Intent
        data object BackClicked : Intent
        data object ShareClicked : Intent
        data object OpenInTmdbClicked : Intent
    }

    sealed interface Effect {
        data object NavigateBack : Effect
        data class ShowMessage(val text: UiText) : Effect

        /** Handing off to another app is a one-off action, never screen state. */
        data class ShareText(val text: String) : Effect
        data class OpenUrl(val url: String) : Effect
    }

    sealed interface Change {
        data object Loading : Change
        data class Loaded(val details: MovieDetails) : Change
        data class Failed(val error: UiText) : Change
        data class FavoriteUpdated(val isFavorite: Boolean) : Change
    }
}
