package com.nikpapajohn.moviedb.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nikpapajohn.moviedb.R
import com.nikpapajohn.moviedb.core.PosterSize
import com.nikpapajohn.moviedb.domain.model.MovieListItem

const val MOVIE_CARD_TAG = "movie_card"

/**
 * The list row from the mockup: poster left, title and rating in the middle,
 * favorite indicator on the right.
 */
@Composable
fun MovieCard(
    item: MovieListItem,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .testTag(MOVIE_CARD_TAG),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PosterImage(
                posterPath = item.movie.posterPath,
                size = PosterSize.LIST,
                contentDescription = stringResource(R.string.cd_poster, item.movie.title),
                modifier = Modifier
                    .width(64.dp)
                    .height(96.dp),
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = item.movie.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                RatingRow(rating = item.movie.rating)
                if (item.movie.genreNames.isNotEmpty()) {
                    Text(
                        text = item.movie.genreNames.joinToString(", "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            FavoriteBookmark(isFavorite = item.isFavorite, onToggle = onToggleFavorite)
        }
    }
}
