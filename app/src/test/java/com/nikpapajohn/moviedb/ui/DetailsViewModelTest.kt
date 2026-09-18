package com.nikpapajohn.moviedb.ui

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.nikpapajohn.moviedb.core.AppError
import com.nikpapajohn.moviedb.core.AppErrorException
import com.nikpapajohn.moviedb.domain.model.Movie
import com.nikpapajohn.moviedb.domain.model.MovieDetails
import com.nikpapajohn.moviedb.domain.usecase.GetMovieDetailsUseCase
import com.nikpapajohn.moviedb.domain.usecase.ObserveIsFavoriteUseCase
import com.nikpapajohn.moviedb.domain.usecase.ToggleFavoriteUseCase
import com.nikpapajohn.moviedb.ui.details.DetailsContract.Effect
import com.nikpapajohn.moviedb.ui.details.DetailsContract.Intent
import com.nikpapajohn.moviedb.ui.details.DetailsViewModel
import com.nikpapajohn.moviedb.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DetailsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getMovieDetails: GetMovieDetailsUseCase = mockk()
    private val observeIsFavorite: ObserveIsFavoriteUseCase = mockk()
    private val toggleFavorite: ToggleFavoriteUseCase = mockk(relaxed = true)

    private val details = MovieDetails(
        id = 278,
        title = "The Shawshank Redemption",
        tagline = "Fear can hold you prisoner.",
        overview = "Two imprisoned men bond over a number of years.",
        posterPath = "/poster.jpg",
        backdropPath = null,
        rating = 8.7,
        voteCount = 26_000,
        releaseDate = "1994-09-23",
        runtimeMinutes = 142,
        genres = emptyList(),
    )

    private fun viewModel() = DetailsViewModel(
        savedStateHandle = SavedStateHandle(mapOf("movieId" to 278)),
        getMovieDetails = getMovieDetails,
        observeIsFavorite = observeIsFavorite,
        toggleFavorite = toggleFavorite,
    )

    @Test
    fun `load puts the details in the state and stops loading`() = runTest {
        coEvery { getMovieDetails(278) } returns Result.success(details)
        every { observeIsFavorite(278) } returns flowOf(false)

        val viewModel = viewModel()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals("The Shawshank Redemption", state.details?.title)
        assertEquals(null, state.error)
    }

    @Test
    fun `a failure lands in the state instead of throwing`() = runTest {
        coEvery { getMovieDetails(278) } returns Result.failure(AppErrorException(AppError.Network))
        every { observeIsFavorite(278) } returns flowOf(false)

        val viewModel = viewModel()
        advanceUntilIdle()

        assertTrue(viewModel.state.value.error != null)
    }

    @Test
    fun `retry after an error reaches a loaded state`() = runTest {
        coEvery { getMovieDetails(278) } returnsMany listOf(
            Result.failure(AppErrorException(AppError.Network)),
            Result.success(details),
        )
        every { observeIsFavorite(278) } returns flowOf(false)

        val viewModel = viewModel()
        advanceUntilIdle()
        viewModel.onIntent(Intent.Retry)
        advanceUntilIdle()

        assertEquals(details, viewModel.state.value.details)
        assertEquals(null, viewModel.state.value.error)
    }

    @Test
    fun `the favorite flag follows the store, not the button`() = runTest {
        val isFavorite = MutableStateFlow(false)
        coEvery { getMovieDetails(278) } returns Result.success(details)
        every { observeIsFavorite(278) } returns isFavorite
        coEvery { toggleFavorite(any()) } coAnswers {
            isFavorite.value = !isFavorite.value
            isFavorite.value
        }

        val viewModel = viewModel()
        advanceUntilIdle()
        assertFalse(viewModel.state.value.isFavorite)

        viewModel.onIntent(Intent.ToggleFavorite)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.isFavorite)
        coVerify { toggleFavorite(match<Movie> { it.id == 278 }) }
    }

    @Test
    fun `toggling announces the result as a one-off message`() = runTest {
        coEvery { getMovieDetails(278) } returns Result.success(details)
        every { observeIsFavorite(278) } returns flowOf(false)
        coEvery { toggleFavorite(any()) } returns true

        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.effects.test {
            viewModel.onIntent(Intent.ToggleFavorite)
            advanceUntilIdle()
            assertTrue(awaitItem() is Effect.ShowMessage)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `back is a navigation effect, not a state change`() = runTest {
        coEvery { getMovieDetails(278) } returns Result.success(details)
        every { observeIsFavorite(278) } returns flowOf(false)

        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.effects.test {
            viewModel.onIntent(Intent.BackClicked)
            advanceUntilIdle()
            assertEquals(Effect.NavigateBack, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
