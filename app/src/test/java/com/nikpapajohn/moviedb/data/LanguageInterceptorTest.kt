package com.nikpapajohn.moviedb.data

import com.nikpapajohn.moviedb.data.remote.LanguageInterceptor
import com.nikpapajohn.moviedb.data.remote.languageForLocale
import java.util.Locale
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class LanguageInterceptorTest {

    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `greek locale maps to TMDB's el-GR`() {
        assertEquals("el-GR", languageForLocale(Locale("el")))
    }

    @Test
    fun `any other locale falls back to en-US`() {
        assertEquals("en-US", languageForLocale(Locale("fr")))
    }

    @Test
    fun `the language is read fresh on every request, not cached once`() {
        // A value fixed once (the old Hilt @Singleton) could not reflect a change like
        // this between two calls; asking currentLanguage() per request can.
        var current = "en-US"
        val client = OkHttpClient.Builder()
            .addInterceptor(LanguageInterceptor(currentLanguage = { current }))
            .build()
        server.enqueue(MockResponse().setResponseCode(200))
        server.enqueue(MockResponse().setResponseCode(200))

        client.newCall(Request.Builder().url(server.url("/movie/1")).build()).execute().close()
        current = "el-GR"
        client.newCall(Request.Builder().url(server.url("/movie/1")).build()).execute().close()

        assertEquals("/movie/1?language=en-US", server.takeRequest().path)
        assertEquals("/movie/1?language=el-GR", server.takeRequest().path)
    }
}
