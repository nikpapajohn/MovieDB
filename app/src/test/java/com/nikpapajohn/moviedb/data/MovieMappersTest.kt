package com.nikpapajohn.moviedb.data

import com.nikpapajohn.moviedb.data.mapper.toDomain
import com.nikpapajohn.moviedb.data.remote.dto.GenreDto
import com.nikpapajohn.moviedb.data.remote.dto.MovieDetailsDto
import com.nikpapajohn.moviedb.data.remote.dto.MovieDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MovieMappersTest {

    @Test
    fun `movie falls back to the original title when the localised title is missing`() {
        val dto = MovieDto(id = 1, title = null, originalTitle = "Le Samourai", voteAverage = 8.1)

        val movie = dto.toDomain()

        assertEquals("Le Samourai", movie.title)
        assertEquals(8.1, movie.rating, 0.001)
    }

    @Test
    fun `blank poster path becomes null so the UI can show its placeholder`() {
        val movie = MovieDto(id = 2, title = "No Poster", posterPath = "").toDomain()

        assertNull(movie.posterPath)
    }

    @Test
    fun `missing rating is zero rather than a crash`() {
        val movie = MovieDto(id = 3, title = "Unrated", voteAverage = null).toDomain()

        assertEquals(0.0, movie.rating, 0.001)
    }

    @Test
    fun `genre names are attached from the cache`() {
        val movie = MovieDto(id = 4, title = "Heat", genreIds = listOf(28, 80))
            .toDomain(genreNames = listOf("Action", "Crime"))

        assertEquals(listOf("Action", "Crime"), movie.genreNames)
    }

    @Test
    fun `details map runtime zero to null and expose the release year`() {
        val dto = MovieDetailsDto(
            id = 5,
            title = "The Shawshank Redemption",
            overview = "Two imprisoned men bond over a number of years.",
            voteAverage = 8.7,
            voteCount = 26_000,
            releaseDate = "1994-09-23",
            runtime = 0,
            genres = listOf(GenreDto(18, "Drama"))
        )

        val details = dto.toDomain()

        assertNull(details.runtimeMinutes)
        assertEquals("1994", details.releaseYear)
        assertEquals(listOf("Drama"), details.genres.map { it.name })
    }

    @Test
    fun `details with an empty release date expose no year`() {
        val details = MovieDetailsDto(id = 6, title = "Untitled", releaseDate = "").toDomain()

        assertNull(details.releaseDate)
        assertNull(details.releaseYear)
    }
}
