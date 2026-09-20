package com.nikpapajohn.moviedb.ui

import app.cash.turbine.test
import com.nikpapajohn.moviedb.core.AppError
import com.nikpapajohn.moviedb.core.AppErrorException
import com.nikpapajohn.moviedb.domain.model.Movie
import com.nikpapajohn.moviedb.domain.model.MoviePage
import com.nikpapajohn.moviedb.domain.usecase.GetPopularMoviesUseCase
import com.nikpapajohn.moviedb.domain.usecase.ObserveFavoriteIdsUseCase
import com.nikpapajohn.moviedb.domain.usecase.SearchMoviesUseCase
import com.nikpapajohn.moviedb.domain.usecase.ToggleFavoriteUseCase
import com.nikpapajohn.moviedb.ui.popular.PopularContract.Effect
import com.nikpapajohn.moviedb.ui.popular.PopularContract.Intent
import com.nikpapajohn.moviedb.ui.popular.PopularViewModel
import com.nikpapajohn.moviedb.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PopularViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getPopularMovies: GetPopularMoviesUseCase = mockk()
    private val searchMovies: SearchMoviesUseCase = mockk()
    private val observeFavoriteIds: ObserveFavoriteIdsUseCase = mockk()
    private val toggleFavorite: ToggleFavoriteUseCase = mockk(relaxed = true)

    private val favoriteIds = MutableStateFlow<Set<Int>>(emptySet())

    private fun movie(id: Int) =
        Movie(id = id, title = "Movie $id", posterPath = null, rating = 8.0)

    private fun page(number: Int, ids: List<Int>, total: Int = 3) =
        MoviePage(page = number, movies = ids.map(::movie), totalPages = total)

    private fun viewModel() = PopularViewModel(
        getPopularMovies = getPopularMovies,
        searchMovies = searchMovies,
        observeFavoriteIds = observeFavoriteIds,
        toggleFavorite = toggleFavorite
    )

    @Test
    fun `the first page loads on start`() = runTest {
        every { observeFavoriteIds() } returns favoriteIds
        coEvery { getPopularMovies(1) } returns Result.success(page(1, listOf(1, 2)))

        val viewModel = viewModel()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(listOf(1, 2), state.items.map { it.movie.id })
        assertFalse(state.isLoading)
    }

    @Test
    fun `the next page is appended, not replaced`() = runTest {
        every { observeFavoriteIds() } returns favoriteIds
        coEvery { getPopularMovies(1) } returns Result.success(page(1, listOf(1, 2)))
        coEvery { getPopularMovies(2) } returns Result.success(page(2, listOf(3)))

        val viewModel = viewModel()
        advanceUntilIdle()
        viewModel.onIntent(Intent.LoadNextPage)
        advanceUntilIdle()

        assertEquals(listOf(1, 2, 3), viewModel.state.value.items.map { it.movie.id })
        assertEquals(2, viewModel.state.value.page)
    }

    @Test
    fun `the last page stops further loading`() = runTest {
        every { observeFavoriteIds() } returns favoriteIds
        coEvery { getPopularMovies(1) } returns Result.success(page(1, listOf(1), total = 1))

        val viewModel = viewModel()
        advanceUntilIdle()
        viewModel.onIntent(Intent.LoadNextPage)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.endReached)
        coVerify(exactly = 0) { getPopularMovies(2) }
    }

    @Test
    fun `a failure becomes an error in the state`() = runTest {
        every { observeFavoriteIds() } returns favoriteIds
        coEvery { getPopularMovies(1) } returns Result.failure(AppErrorException(AppError.Network))

        val viewModel = viewModel()
        advanceUntilIdle()

        assertTrue(viewModel.state.value.error != null)
        assertTrue(viewModel.state.value.items.isEmpty())
    }

    @Test
    fun `a favorite added elsewhere marks the row without reloading`() = runTest {
        every { observeFavoriteIds() } returns favoriteIds
        coEvery { getPopularMovies(1) } returns Result.success(page(1, listOf(1, 2)))

        val viewModel = viewModel()
        advanceUntilIdle()

        favoriteIds.value = setOf(2)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.items.first { it.movie.id == 2 }.isFavorite)
        coVerify(exactly = 1) { getPopularMovies(1) }
    }

    @Test
    fun `typing a query searches instead of listing popular`() = runTest {
        every { observeFavoriteIds() } returns favoriteIds
        coEvery { getPopularMovies(1) } returns Result.success(page(1, listOf(1)))
        coEvery { searchMovies("heat", 1) } returns Result.success(page(1, listOf(9)))

        val viewModel = viewModel()
        advanceUntilIdle()
        viewModel.onIntent(Intent.QueryChanged("heat"))
        advanceUntilIdle()

        assertEquals(listOf(9), viewModel.state.value.items.map { it.movie.id })
    }

    @Test
    fun `clicking a movie emits a navigation effect exactly once`() = runTest {
        every { observeFavoriteIds() } returns favoriteIds
        coEvery { getPopularMovies(1) } returns Result.success(page(1, listOf(1)))

        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.effects.test {
            viewModel.onIntent(Intent.MovieClicked(1))
            advanceUntilIdle()
            assertEquals(Effect.NavigateToDetails(1), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
