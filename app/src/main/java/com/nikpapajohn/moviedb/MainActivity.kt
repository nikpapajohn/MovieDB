package com.nikpapajohn.moviedb

import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.nikpapajohn.moviedb.ui.MovieDbApp
import com.nikpapajohn.moviedb.ui.theme.MovieDbTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // The top app bar draws behind the status bar, so the bar itself stays transparent
        // and its icons are forced light to stay readable on the teal.
        // SystemBarStyle.auto leaves navigationBarContrastEnforced on, and the system then
        // paints its own scrim behind the bar - the light strip that shows beside the content
        // in landscape. light() and dark() both turn that off, so the style is chosen from the
        // current night mode instead: dark icons on our light surface, light icons in dark theme.
        val darkTheme = resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = if (darkTheme) {
                SystemBarStyle.dark(Color.TRANSPARENT)
            } else {
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
            }
        )
        setContent {
            MovieDbTheme {
                MovieDbApp()
            }
        }
    }
}
