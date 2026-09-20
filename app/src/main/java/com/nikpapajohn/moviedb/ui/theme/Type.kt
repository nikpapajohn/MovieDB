package com.nikpapajohn.moviedb.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Material 3 defaults, with the two roles the mockup calls out: a heavier title and a
 * slightly roomier body for the overview paragraph.
 */
val MovieDbTypography = Typography().let { base ->
    base.copy(
        titleLarge = base.titleLarge.copy(fontWeight = FontWeight.Bold),
        titleMedium = base.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        bodyMedium = base.bodyMedium.copy(lineHeight = 22.sp)
    )
}
