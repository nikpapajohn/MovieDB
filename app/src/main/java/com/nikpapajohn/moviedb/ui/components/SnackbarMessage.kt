package com.nikpapajohn.moviedb.ui.components

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.nikpapajohn.moviedb.core.UiText
import com.nikpapajohn.moviedb.ui.asString

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
