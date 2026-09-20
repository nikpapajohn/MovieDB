package com.nikpapajohn.moviedb.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nikpapajohn.moviedb.R
import com.nikpapajohn.moviedb.ui.theme.MovieDbTheme

const val FAVORITE_BUTTON_TAG = "details_favorite_button"

/**
 * The two states from the mockup: a filled pill with a check mark once favorited, an
 * outlined pill with a hollow heart otherwise.
 */
@Composable
fun FavoriteButton(
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val label = stringResource(
        if (isFavorite) R.string.details_added_favorite else R.string.details_add_favorite,
    )
    // No fixed height: a long label (Greek, or a large system font scale) has to wrap
    // instead of being clipped.
    val buttonModifier = modifier
        .widthIn(max = 320.dp)
        .fillMaxWidth()
        .testTag(FAVORITE_BUTTON_TAG)
    val contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
    val iconSize = Modifier.size(18.dp)

    if (isFavorite) {
        Button(
            onClick = onToggleFavorite,
            modifier = buttonModifier,
            shape = CircleShape,
            contentPadding = contentPadding,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        ) {
            Icon(Icons.Filled.Favorite, contentDescription = null, modifier = iconSize)
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center,
                maxLines = 2,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 6.dp),
            )
            Icon(Icons.Filled.Check, contentDescription = null, modifier = iconSize)
        }
    } else {
        OutlinedButton(
            onClick = onToggleFavorite,
            modifier = buttonModifier,
            shape = CircleShape,
            contentPadding = contentPadding,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.primary,
            ),
        ) {
            Icon(Icons.Outlined.FavoriteBorder, contentDescription = null, modifier = iconSize)
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center,
                maxLines = 2,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 6.dp),
            )
        }
    }
}

@Preview(name = "Favorited", showBackground = true)
@Composable
private fun FavoriteButtonFavoritedPreview() {
    MovieDbTheme {
        FavoriteButton(isFavorite = true, onToggleFavorite = {})
    }
}

@Preview(name = "Not favorited", showBackground = true)
@Composable
private fun FavoriteButtonNotFavoritedPreview() {
    MovieDbTheme {
        FavoriteButton(isFavorite = false, onToggleFavorite = {})
    }
}
