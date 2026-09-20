package com.nikpapajohn.moviedb.ui

import com.nikpapajohn.moviedb.core.UiText
import com.nikpapajohn.moviedb.domain.model.Movie
import com.nikpapajohn.moviedb.domain.model.MovieListItem
import com.nikpapajohn.moviedb.ui.popular.PopularContract.Change
import com.nikpapajohn.moviedb.ui.popular.PopularContract.State
import com.nikpapajohn.moviedb.ui.popular.PopularReducer.reduce
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The reducer is pure, so every screen transition is testable without coroutines,
 * mocks or Android. This is the main reason the screen is written as MVI.
 */
class PopularReducerTest {

    private fun movie(id: Int, title: String = "Movie $id") =
        Movie(id = id, title = title, posterPath = null, rating = 7.5)

    @Test
    fun `first page loading clears previous errors`() {
        val state = State(error = UiText.Dynamic("boom"), appendError = UiText.Dynamic("boom"))

        val result = reduce(state, Change.FirstPageLoading)

        assertTrue(result.isLoading)
        assertEquals(null, result.error)
        assertEquals(null, result.appendError)
    }

    @Test
    fun `a replacing page swaps the list and stops loading`() {
        val state = State(items = listOf(MovieListItem(movie(1), false)), isLoading = true)

        val result = reduce(
            state,
            Change.PageLoaded(
                movies = listOf(movie(2)),
                page = 1,
                endReached = false,
                replace = true
            )
        )

        assertEquals(listOf(2), result.items.map { it.movie.id })
        assertEquals(1, result.page)
        assertFalse(result.isLoading)
    }

    @Test
    fun `an appended page keeps the previous items`() {
        val state = State(items = listOf(MovieListItem(movie(1), false)), page = 1)

        val result = reduce(
            state,
            Change.PageLoaded(
                movies = listOf(movie(2)),
                page = 2,
                endReached = false,
                replace = false
            )
        )

        assertEquals(listOf(1, 2), result.items.map { it.movie.id })
        assertEquals(2, result.page)
    }

    @Test
    fun `a movie repeated across pages is not added twice`() {
        val state = State(items = listOf(MovieListItem(movie(1), false)), page = 1)

        val result = reduce(
            state,
            Change.PageLoaded(
                movies = listOf(movie(1), movie(2)),
                page = 2,
                endReached = false,
                replace = false
            )
        )

        assertEquals(listOf(1, 2), result.items.map { it.movie.id })
    }

    @Test
    fun `a new page respects favorites that are already known`() {
        val state = State(favoriteIds = setOf(2))

        val result = reduce(
            state,
            Change.PageLoaded(
                movies = listOf(movie(1), movie(2)),
                page = 1,
                endReached = true,
                replace = true
            )
        )

        assertFalse(result.items.first { it.movie.id == 1 }.isFavorite)
        assertTrue(result.items.first { it.movie.id == 2 }.isFavorite)
        assertTrue(result.endReached)
    }

    @Test
    fun `a first page failure empties the list, an append failure keeps it`() {
        val loaded = State(items = listOf(MovieListItem(movie(1), false)))

        val firstPageFailure = reduce(
            loaded,
            Change.LoadFailed(UiText.Dynamic("network"), isAppend = false)
        )
        val appendFailure = reduce(
            loaded,
            Change.LoadFailed(UiText.Dynamic("network"), isAppend = true)
        )

        assertTrue(firstPageFailure.items.isEmpty())
        assertEquals(UiText.Dynamic("network"), firstPageFailure.error)

        assertEquals(1, appendFailure.items.size)
        assertEquals(null, appendFailure.error)
        assertEquals(UiText.Dynamic("network"), appendFailure.appendError)
    }

    @Test
    fun `favorites updates re-mark the visible list`() {
        val state =
            State(items = listOf(MovieListItem(movie(1), false), MovieListItem(movie(2), true)))

        val result = reduce(state, Change.FavoritesUpdated(setOf(1)))

        assertTrue(result.items.first { it.movie.id == 1 }.isFavorite)
        assertFalse(result.items.first { it.movie.id == 2 }.isFavorite)
    }

    @Test
    fun `closing the search box clears the query`() {
        val opened = reduce(State(), Change.SearchToggled)
        val typed = reduce(opened, Change.QueryUpdated("shawshank"))

        val closed = reduce(typed, Change.SearchToggled)

        assertTrue(opened.isSearchVisible)
        assertEquals("shawshank", typed.query)
        assertFalse(closed.isSearchVisible)
        assertEquals("", closed.query)
    }

    @Test
    fun `an append is not started while one is already running or the end is reached`() {
        val items = listOf(MovieListItem(movie(1), false))

        assertFalse(State(items = items, isLoadingMore = true).canLoadMore)
        assertFalse(State(items = items, endReached = true).canLoadMore)
        assertFalse(State(items = emptyList()).canLoadMore)
        assertTrue(State(items = items).canLoadMore)
    }
}
