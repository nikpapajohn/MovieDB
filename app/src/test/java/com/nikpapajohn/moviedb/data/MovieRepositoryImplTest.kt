package com.nikpapajohn.moviedb.data

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.nikpapajohn.moviedb.core.AppError
import com.nikpapajohn.moviedb.core.toAppError
import com.nikpapajohn.moviedb.data.remote.GenreCache
import com.nikpapajohn.moviedb.data.remote.TmdbApi
import com.nikpapajohn.moviedb.data.repository.MovieRepositoryImpl
import com.nikpapajohn.moviedb.util.TestDispatcherProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit

@OptIn(ExperimentalCoroutinesApi::class)
class MovieRepositoryImplTest {

    private lateinit var server: MockWebServer
    private lateinit var api: TmdbApi

    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(TmdbApi::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    /**
     * Built inside the test so the repository's dispatcher shares runTest's scheduler.
     * A TestDispatcher created outside runTest brings its own scheduler, and the two
     * clash the moment the repository calls withContext.
     */
    private fun TestScope.repository() = MovieRepositoryImpl(
        api = api,
        genreCache = GenreCache(api),
        dispatchers = TestDispatcherProvider(StandardTestDispatcher(testScheduler)),
        language = "en-US",
    )

    @Test
    fun `details are mapped from the TMDB payload`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """
                {
                  "id": 278,
                  "title": "The Shawshank Redemption",
                  "overview": "Two imprisoned men bond over a number of years.",
                  "poster_path": "/q6y0Go.jpg",
                  "vote_average": 8.7,
                  "vote_count": 26000,
                  "release_date": "1994-09-23",
                  "runtime": 142,
                  "genres": [{"id": 18, "name": "Drama"}]
                }
                """.trimIndent(),
            ),
        )

        val details = repository().movieDetails(278).getOrThrow()

        assertEquals("The Shawshank Redemption", details.title)
        assertEquals(142, details.runtimeMinutes)
        assertEquals("1994", details.releaseYear)
        assertEquals("/movie/278?language=en-US", server.takeRequest().path)
    }

    @Test
    fun `a popular page is mapped with its pagination info`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """
                {
                  "page": 2,
                  "total_pages": 500,
                  "total_results": 10000,
                  "results": [
                    {"id": 1, "title": "First", "vote_average": 7.1, "genre_ids": [18]},
                    {"id": 2, "title": "Second", "poster_path": "/p.jpg", "vote_average": 6.4}
                  ]
                }
                """.trimIndent(),
            ),
        )
        // the genre lookup the mapper triggers for the first movie
        server.enqueue(
            MockResponse().setResponseCode(200).setBody("""{"genres":[{"id":18,"name":"Drama"}]}"""),
        )

        val page = repository().popularMovies(2).getOrThrow()

        assertEquals(2, page.page)
        assertEquals(500, page.totalPages)
        assertEquals(listOf(1, 2), page.movies.map { it.id })
        assertEquals(listOf("Drama"), page.movies.first().genreNames)
        assertTrue(page.hasMorePages)
    }

    @Test
    fun `the last page reports that there is nothing more to load`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"page": 3, "total_pages": 3, "total_results": 50, "results": []}""",
            ),
        )

        val page = repository().popularMovies(3).getOrThrow()

        assertFalse(page.hasMorePages)
    }

    @Test
    fun `a 401 surfaces as Unauthorized so the UI can point at the token`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(401).setBody("""{"status_message":"Invalid API key"}"""),
        )

        val result = repository().movieDetails(278)

        assertTrue(result.isFailure)
        assertEquals(AppError.Unauthorized, result.exceptionOrNull()!!.toAppError())
    }

    @Test
    fun `a 500 keeps the status code for the error message`() = runTest {
        server.enqueue(MockResponse().setResponseCode(500))

        val error = repository().movieDetails(278).exceptionOrNull()!!.toAppError()

        assertEquals(AppError.Http(500), error)
    }

    @Test
    fun `a malformed body is an Unknown error, not a crash`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody("not json at all"))

        val error = repository().movieDetails(278).exceptionOrNull()!!.toAppError()

        assertTrue(error is AppError.Unknown)
    }
}
