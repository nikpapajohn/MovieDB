package com.nikpapajohn.moviedb.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.nikpapajohn.moviedb.R
import com.nikpapajohn.moviedb.core.AppError
import com.nikpapajohn.moviedb.core.UiText

/** The one place where a failure becomes something a person reads. */
fun AppError.toUiText(): UiText = when (this) {
    AppError.Network -> UiText.res(R.string.error_network)
    AppError.Unauthorized -> UiText.res(R.string.error_unauthorized)
    is AppError.Http -> UiText.res(R.string.error_server, code)
    is AppError.Unknown -> UiText.res(R.string.error_unknown)
}

@Composable
fun UiText.asString(): String = when (this) {
    is UiText.Dynamic -> value
    is UiText.Res -> if (args.isEmpty()) {
        stringResource(id)
    } else {
        stringResource(id, *args.toTypedArray())
    }
}
