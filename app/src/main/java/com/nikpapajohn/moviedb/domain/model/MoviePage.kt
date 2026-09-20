package com.nikpapajohn.moviedb.domain.model

import androidx.compose.runtime.Immutable

/**
 * One TMDB page. Pagination is explicit so the whole screen state stays reducible;
 * see the README for why Paging 3 is not used here.
 */
@Immutable
data class MoviePage(
    val page: Int,
    val movies: List<Movie>,
    val totalPages: Int,
) {
    /** TMDB refuses page numbers above 500, regardless of totalPages. */
    val hasMorePages: Boolean
        get() = page < totalPages && page < MAX_PAGE

    companion object {
        const val FIRST_PAGE = 1
        const val MAX_PAGE = 500
    }
}
