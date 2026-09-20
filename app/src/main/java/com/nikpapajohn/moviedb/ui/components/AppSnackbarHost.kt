package com.nikpapajohn.moviedb.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nikpapajohn.moviedb.ui.theme.MovieDbTheme

// In landscape a full-width Snackbar stretches across the whole screen, which reads as
// oversized. Portrait already looks right at the default width, so the cap only applies
// in landscape.
private val LANDSCAPE_MAX_WIDTH = 360.dp

@Composable
fun AppSnackbarHost(hostState: SnackbarHostState) {
    val isLandscape =
        LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    SnackbarHost(
        hostState = hostState,
        modifier = if (isLandscape) Modifier.widthIn(max = LANDSCAPE_MAX_WIDTH) else Modifier,
    )
}

@Preview(name = "Portrait", showBackground = true)
@Composable
private fun AppSnackbarHostPreview() {
    val hostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) { hostState.showSnackbar("Αφαιρέθηκε από τα αγαπημένα") }
    MovieDbTheme {
        AppSnackbarHost(hostState = hostState)
    }
}

@Preview(name = "Landscape", showBackground = true, widthDp = 640, heightDp = 320)
@Composable
private fun AppSnackbarHostLandscapePreview() {
    val hostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) { hostState.showSnackbar("Αφαιρέθηκε από τα αγαπημένα") }
    MovieDbTheme {
        AppSnackbarHost(hostState = hostState)
    }
}
