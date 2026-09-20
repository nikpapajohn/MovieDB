package com.nikpapajohn.moviedb.ui.components

import android.annotation.SuppressLint
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.nikpapajohn.moviedb.core.UiText
import com.nikpapajohn.moviedb.ui.asString
import com.nikpapajohn.moviedb.ui.theme.MovieDbTheme

/**
 * Resolves a UiText inside composition and shows it once. Keeps string resources out of
 * the ViewModel without pushing snackbar plumbing into every screen.
 */
@Composable
fun SnackbarMessage(
    message: UiText?,
    hostState: SnackbarHostState,
    onShown: () -> Unit,
) {
    val text = message?.asString()
    LaunchedEffect(text) {
        if (text != null) {
            hostState.showSnackbar(text)
            onShown()
        }
    }
}

// SnackbarMessage itself draws nothing — it only pushes text into a SnackbarHostState — so
// the preview wraps it in a Scaffold with a real host to make the effect visible.
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Preview(showBackground = true)
@Composable
private fun SnackbarMessagePreview() {
    val hostState = remember { SnackbarHostState() }
    MovieDbTheme {
        Scaffold(snackbarHost = { AppSnackbarHost(hostState) }) { _ ->
            SnackbarMessage(
                message = UiText.Dynamic("Αφαιρέθηκε από τα αγαπημένα"),
                hostState = hostState,
                onShown = {},
            )
        }
    }
}
