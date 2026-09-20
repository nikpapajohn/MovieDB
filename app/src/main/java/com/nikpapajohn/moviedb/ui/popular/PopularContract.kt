package com.nikpapajohn.moviedb.ui.popular

import androidx.compose.runtime.Immutable
import com.nikpapajohn.moviedb.core.UiText
import com.nikpapajohn.moviedb.domain.model.Movie
import com.nikpapajohn.moviedb.domain.model.MovieListItem

/**
 * The MVI contract of the popular/search screen: what the UI shows (State), what the user
 * can do (Intent), what happens once and is not state (Effect), and the internal results
 * that the reducer folds into the state (Change).
 */
object PopularContract {

    // @Immutable: every instance is produced via copy()/emptyList()/emptySet() in the reducer;
    // nothing here ever mutates a List/Set field in place.
    @Immutable
    data class State(
        val items: List<MovieListItem> = emptyList(),
        val favoriteIds: Set<Int> = emptySet(),
        val query: String = "",
        val isSearchVisible: Boolean = false,
        val isLoading: Boolean = false,
        val isLoadingMore: Boolean = false,
        val error: UiText? = null,
        val appendError: UiText? = null,
        val page: Int = 0,
        val endReached: Boolean = false,
    ) {
        val isEmpty: Boolean
            get() = items.isEmpty() && !isLoading && error == null

        /**
         * The auto-scroll trigger only. The ViewModel spells its own guard out instead of
         * reusing this, because an explicit retry after an append error has to get through
         * while appendError is still set.
         */
        val canLoadMore: Boolean
            get() = !isLoading && !isLoadingMore && !endReached && appendError == null && items.isNotEmpty()
    }

    sealed interface Intent {
        /** First page, for the current query. */
        data object Load : Intent
        data object Retry : Intent
        data object LoadNextPage : Intent
        data class QueryChanged(val value: String) : Intent
        data object SearchToggled : Intent
        data class MovieClicked(val movieId: Int) : Intent
        data class FavoriteToggled(val movie: Movie) : Intent
        data object FavoritesClicked : Intent
    }

    sealed interface Effect {
        data class NavigateToDetails(val movieId: Int) : Effect
        data object NavigateToFavorites : Effect
        data class ShowMessage(val text: UiText) : Effect
    }

    /** Results of work, folded into the state by [PopularReducer]. Never leaves the ViewModel. */
    sealed interface Change {
        data class QueryUpdated(val value: String) : Change
        data object SearchToggled : Change
        data object FirstPageLoading : Change
        data object NextPageLoading : Change
        data class PageLoaded(
            val movies: List<Movie>,
            val page: Int,
            val endReached: Boolean,
            val replace: Boolean,
        ) : Change
        data class LoadFailed(val error: UiText, val isAppend: Boolean) : Change
        data class FavoritesUpdated(val ids: Set<Int>) : Change
    }
}
