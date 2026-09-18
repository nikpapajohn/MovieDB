package com.nikpapajohn.moviedb

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
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        setContent {
            MovieDbTheme {
                MovieDbApp()
            }
        }
    }
}
