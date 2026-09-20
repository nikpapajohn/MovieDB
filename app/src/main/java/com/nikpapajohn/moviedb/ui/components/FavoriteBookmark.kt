package com.nikpapajohn.moviedb.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.nikpapajohn.moviedb.R
import com.nikpapajohn.moviedb.ui.theme.MovieDbTheme

/**
 * Both the favorite indicator and the way to change it, straight from the mockup:
 * you can favorite a movie without opening its details.
 */
@Composable
fun FavoriteBookmark(
    isFavorite: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scale by animateFloatAsState(targetValue = if (isFavorite) 1.1f else 1f, label = "bookmarkScale")
    IconToggleButton(
        checked = isFavorite,
        onCheckedChange = { onToggle() },
        modifier = modifier,
    ) {
        Icon(
            imageVector = if (isFavorite) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
            contentDescription = stringResource(
                if (isFavorite) R.string.cd_remove_favorite else R.string.cd_add_favorite,
            ),
            tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
            modifier = Modifier.scale(scale),
        )
    }
}

@Preview(name = "Favorited", showBackground = true)
@Composable
private fun FavoriteBookmarkFavoritedPreview() {
    MovieDbTheme {
        FavoriteBookmark(isFavorite = true, onToggle = {})
    }
}

@Preview(name = "Not favorited", showBackground = true)
@Composable
private fun FavoriteBookmarkNotFavoritedPreview() {
    MovieDbTheme {
        FavoriteBookmark(isFavorite = false, onToggle = {})
    }
}
