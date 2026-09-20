package com.nikpapajohn.moviedb.domain

import com.nikpapajohn.moviedb.domain.model.Movie
import com.nikpapajohn.moviedb.domain.model.MovieDetails
import com.nikpapajohn.moviedb.domain.model.MoviePage
import com.nikpapajohn.moviedb.domain.repository.FavoritesRepository
import com.nikpapajohn.moviedb.domain.repository.MovieRepository
import com.nikpapajohn.moviedb.domain.usecase.ClearFavoritesUseCase
import com.nikpapajohn.moviedb.domain.usecase.GetMovieDetailsUseCase
import com.nikpapajohn.moviedb.domain.usecase.GetPopularMoviesUseCase
import com.nikpapajohn.moviedb.domain.usecase.ObserveFavoriteIdsUseCase
import com.nikpapajohn.moviedb.domain.usecase.ObserveFavoritesUseCase
import com.nikpapajohn.moviedb.domain.usecase.ObserveIsFavoriteUseCase
import com.nikpapajohn.moviedb.domain.usecase.SearchMoviesUseCase
import com.nikpapajohn.moviedb.domain.usecase.ToggleFavoriteUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Every use case here is a one-line delegation to a repository. There is no branching to
 * exercise, so each test only proves two things: the call reaches the right repository
 * method with the right arguments, and the result comes back unchanged. Thin as they are,
 * a typo swapping two repository calls (e.g. search calling popularMovies) would otherwise
 * go unnoticed until it broke a screen.
 */
class UseCasesTest {

    private val movieRepository: MovieRepository = mockk()
    private val favoritesRepository: FavoritesRepository = mockk()

    private val movie = Movie(id = 1, title = "Arrival", posterPath = "/p.jpg", rating = 7.9)
    private val moviePage = MoviePage(page = 1, movies = listOf(movie), totalPages = 10)
    private val details = MovieDetails(
        id = 1,
        title = "Arrival",
        tagline = null,
        overview = "First contact.",
        posterPath = "/p.jpg",
        rating = 7.9,
        voteCount = 100,
        releaseDate = "2016-11-10",
        runtimeMinutes = 116,
        genres = emptyList()
    )

    @Test
    fun `get popular movies delegates to the repository's page`() = runTest {
        coEvery { movieRepository.popularMovies(2) } returns Result.success(moviePage)

        val result = GetPopularMoviesUseCase(movieRepository)(2)

        assertEquals(Result.success(moviePage), result)
        coVerify { movieRepository.popularMovies(2) }
    }

    @Test
    fun `search movies delegates the query and page`() = runTest {
        coEvery { movieRepository.searchMovies("dune", 1) } returns Result.success(moviePage)

        val result = SearchMoviesUseCase(movieRepository)("dune", 1)

        assertEquals(Result.success(moviePage), result)
        coVerify { movieRepository.searchMovies("dune", 1) }
    }

    @Test
    fun `get movie details delegates the id`() = runTest {
        coEvery { movieRepository.movieDetails(1) } returns Result.success(details)

        val result = GetMovieDetailsUseCase(movieRepository)(1)

        assertEquals(Result.success(details), result)
        coVerify { movieRepository.movieDetails(1) }
    }

    @Test
    fun `observe favorite ids exposes the repository's flow`() = runTest {
        every { favoritesRepository.favoriteIds() } returns flowOf(setOf(1, 2))

        val ids = ObserveFavoriteIdsUseCase(favoritesRepository)().let { flow ->
            var last = emptySet<Int>()
            flow.collect { last = it }
            last
        }

        assertEquals(setOf(1, 2), ids)
    }

    @Test
    fun `observe favorites exposes the repository's flow`() = runTest {
        every { favoritesRepository.favorites() } returns flowOf(listOf(movie))

        var last = emptyList<Movie>()
        ObserveFavoritesUseCase(favoritesRepository)().collect { last = it }

        assertEquals(listOf(movie), last)
    }

    @Test
    fun `observe is favorite delegates the movie id`() = runTest {
        every { favoritesRepository.isFavorite(1) } returns flowOf(true)

        var last = false
        ObserveIsFavoriteUseCase(favoritesRepository)(1).collect { last = it }

        assertTrue(last)
    }

    @Test
    fun `toggle favorite returns the state after the toggle`() = runTest {
        coEvery { favoritesRepository.toggle(movie) } returns true

        val result = ToggleFavoriteUseCase(favoritesRepository)(movie)

        assertTrue(result)
        coVerify { favoritesRepository.toggle(movie) }
    }

    @Test
    fun `clear favorites delegates to the repository`() = runTest {
        coEvery { favoritesRepository.clear() } returns Unit

        ClearFavoritesUseCase(favoritesRepository)()

        coVerify { favoritesRepository.clear() }
    }
}
