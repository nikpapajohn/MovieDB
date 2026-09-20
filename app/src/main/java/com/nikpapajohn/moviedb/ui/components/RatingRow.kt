package com.nikpapajohn.moviedb.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nikpapajohn.moviedb.R
import com.nikpapajohn.moviedb.ui.theme.MovieDbTheme
import com.nikpapajohn.moviedb.ui.theme.RatingAmber
import java.util.Locale

@Composable
fun RatingRow(
    rating: Double,
    modifier: Modifier = Modifier,
    voteCount: Int? = null,
    starSize: Int = 18,
    ratingStyle: TextStyle = MaterialTheme.typography.titleSmall,
    voteCountStyle: TextStyle = MaterialTheme.typography.labelSmall,
) {
    val formatted = String.format(Locale.getDefault(), "%.1f", rating)
    val description = stringResource(R.string.cd_rating, formatted)
    Row(
        modifier = modifier.clearAndSetSemantics { contentDescription = description },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = null,
            tint = RatingAmber,
            modifier = Modifier.size(starSize.dp),
        )
        // Baseline alignment, not centre: the vote count has to sit on the same line as the
        // rating even though it is several sizes smaller.
        Text(
            text = formatted,
            style = ratingStyle,
            modifier = Modifier.alignByBaseline(),
        )
        if (voteCount != null && voteCount > 0) {
            Text(
                text = stringResource(R.string.details_votes, formatVotes(voteCount)),
                style = voteCountStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.alignByBaseline(),
            )
        }
    }
}

private fun formatVotes(count: Int): String =
    java.text.NumberFormat.getIntegerInstance(Locale.getDefault()).format(count)

@Preview(name = "Rating only", showBackground = true)
@Composable
private fun RatingRowPreview() {
    MovieDbTheme {
        RatingRow(rating = 8.7)
    }
}

@Preview(name = "With vote count", showBackground = true)
@Composable
private fun RatingRowWithVotesPreview() {
    MovieDbTheme {
        RatingRow(rating = 8.7, voteCount = 26000, starSize = 18)
    }
}
