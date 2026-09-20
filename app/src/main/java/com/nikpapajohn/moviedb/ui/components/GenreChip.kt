package com.nikpapajohn.moviedb.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nikpapajohn.moviedb.ui.theme.MovieDbTheme

/**
 * A single genre pill, as the mockup draws it. Hand-rolled instead of AssistChip: the
 * Material chip has a fixed 32dp height and its own padding, both larger than the label
 * this design calls for.
 */
@Composable
fun GenreChip(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}

@Preview(showBackground = true)
@Composable
private fun GenreChipPreview() {
    MovieDbTheme {
        GenreChip(text = "Drama")
    }
}
