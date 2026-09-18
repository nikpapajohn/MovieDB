package com.nikpapajohn.moviedb.ui

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.nikpapajohn.moviedb.R
import com.nikpapajohn.moviedb.domain.model.Movie
import com.nikpapajohn.moviedb.domain.model.MovieListItem
import com.nikpapajohn.moviedb.ui.components.MovieCard
import com.nikpapajohn.moviedb.ui.theme.MovieDbTheme
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Every expected string comes from resources, never from a literal: the app ships English
 * and Greek, and the test has to pass on a device set to either one.
 */
class MovieCardTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context: Context = ApplicationProvider.getApplicationContext()

    private val item = MovieListItem(
        movie = Movie(
            id = 278,
            title = "The Shawshank Redemption",
            posterPath = null,
            rating = 8.7,
            genreNames = listOf("Drama"),
        ),
        isFavorite = false,
    )

    private fun ratingDescription(rating: Double): String =
        context.getString(R.string.cd_rating, String.format(Locale.getDefault(), "%.1f", rating))

    @Test
    fun shows_title_genres_and_the_rating_as_an_accessible_label() {
        composeRule.setContent {
            MovieDbTheme { MovieCard(item = item, onClick = {}, onToggleFavorite = {}) }
        }

        composeRule.onNodeWithText("The Shawshank Redemption").assertIsDisplayed()
        composeRule.onNodeWithText("Drama").assertIsDisplayed()
        composeRule.onNodeWithContentDescription(ratingDescription(8.7)).assertExists()
    }

    @Test
    fun tapping_the_card_reports_the_click() {
        var clicked = 0
        composeRule.setContent {
            MovieDbTheme { MovieCard(item = item, onClick = { clicked++ }, onToggleFavorite = {}) }
        }

        composeRule.onNodeWithText("The Shawshank Redemption").performClick()

        assertEquals(1, clicked)
    }

    @Test
    fun the_bookmark_reports_the_toggle_and_reads_out_its_state() {
        var toggled = false
        composeRule.setContent {
            MovieDbTheme {
                MovieCard(item = item, onClick = {}, onToggleFavorite = { toggled = true })
            }
        }

        composeRule
            .onNodeWithContentDescription(context.getString(R.string.cd_add_favorite))
            .performClick()

        assertTrue(toggled)
    }

    @Test
    fun a_favorited_card_reads_out_the_opposite_action() {
        composeRule.setContent {
            MovieDbTheme {
                MovieCard(
                    item = item.copy(isFavorite = true),
                    onClick = {},
                    onToggleFavorite = {},
                )
            }
        }

        composeRule
            .onNodeWithContentDescription(context.getString(R.string.cd_remove_favorite))
            .assertExists()
    }
}
