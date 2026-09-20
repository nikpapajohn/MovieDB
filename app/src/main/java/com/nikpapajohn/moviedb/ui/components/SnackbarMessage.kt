package com.nikpapajohn.moviedb.ui.components

import android.annotation.SuppressLint
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import com.nikpapajohn.moviedb.core.UiText
import com.nikpapajohn.moviedb.ui.asString
import com.nikpapajohn.moviedb.ui.theme.MovieDbTheme

/**
 * One request to show a snackbar. The [id] is what makes it an event rather than a value:
 * favoriting and unfavoriting the same movie twice produces two identical texts, and
 * without an identity of its own the second one would look like the first and be dropped.
 */
@Immutable
data class SnackbarRequest(val text: UiText, val id: Long)

/** Hands out [SnackbarRequest]s with increasing ids, so no two are ever equal. */
@Stable
class SnackbarController {
    var current by mutableStateOf<SnackbarRequest?>(null)
        private set

    private var nextId = 0L

    fun show(text: UiText) {
        current = SnackbarRequest(text, nextId++)
    }

    fun consume() {
        current = null
    }
}

@Composable
fun rememberSnackbarController(): SnackbarController = remember { SnackbarController() }

/**
 * Resolves a UiText inside composition and shows it once. Keeps string resources out of
 * the ViewModel without pushing snackbar plumbing into every screen.
 */
@Composable
fun SnackbarMessage(message: SnackbarRequest?, hostState: SnackbarHostState, onShown: () -> Unit) {
    val text = message?.text?.asString()
    // Keyed on the id, not the text. showSnackbar suspends until the snackbar is dismissed
    // and onShown only runs afterwards, so a second identical message arriving in the
    // meantime would leave the key unchanged and never be shown at all.
    LaunchedEffect(message?.id) {
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
                message = SnackbarRequest(UiText.Dynamic("Αφαιρέθηκε από τα αγαπημένα"), id = 0),
                hostState = hostState,
                onShown = {}
            )
        }
    }
}
