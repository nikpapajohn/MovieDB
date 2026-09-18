package com.nikpapajohn.moviedb.core

import androidx.annotation.StringRes

/**
 * A message a ViewModel can produce without holding a Context: either a literal string
 * (usually from a server) or a string resource with arguments, resolved in the UI layer.
 */
sealed interface UiText {
    data class Dynamic(val value: String) : UiText

    data class Res(@StringRes val id: Int, val args: List<Any> = emptyList()) : UiText

    companion object {
        fun res(@StringRes id: Int, vararg args: Any): Res = Res(id, args.toList())
    }
}
