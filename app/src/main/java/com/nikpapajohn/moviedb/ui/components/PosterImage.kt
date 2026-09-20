package com.nikpapajohn.moviedb.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.nikpapajohn.moviedb.core.PosterSize
import com.nikpapajohn.moviedb.core.posterUrl
import com.nikpapajohn.moviedb.ui.theme.MovieDbTheme

/**
 * TMDB often has no poster for a title, so "missing" is a first-class state here
 * instead of an empty grey box.
 */
@Composable
fun PosterImage(
    posterPath: String?,
    size: PosterSize,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 8.dp,
) {
    val url = posterUrl(posterPath, size)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        if (url == null) {
            Icon(
                imageVector = Icons.Outlined.Movie,
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.outline,
            )
        } else {
            SubcomposeAsyncImage(
                model = url,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                loading = {},
                error = {
                    Icon(
                        imageVector = Icons.Outlined.Movie,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                    )
                },
            )
        }
    }
}

// A poster URL is never reachable inside the preview renderer, so the "missing poster"
// state (null path) is the only one that reliably shows anything but the error icon.
@Preview(name = "Missing poster", showBackground = true)
@Composable
private fun PosterImageMissingPreview() {
    MovieDbTheme {
        PosterImage(
            posterPath = null,
            size = PosterSize.LIST,
            contentDescription = null,
            modifier = Modifier.width(96.dp).height(144.dp),
        )
    }
}
