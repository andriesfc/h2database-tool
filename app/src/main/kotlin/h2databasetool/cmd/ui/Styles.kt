package h2databasetool.cmd.ui

import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextColors.Companion.rgb
import com.github.ajalt.mordant.rendering.TextStyles
import com.github.ajalt.mordant.rendering.TextStyles.bold

data object Styles {

    data object Colors {
        val illuminatingEmerald = rgb("#359570FF")
    }

    val boldEmphasis = bold
    val notice = Colors.illuminatingEmerald + bold
    val softFocus = TextStyles.dim
    val softFocusError = softFocus + TextColors.yellow + TextStyles.italic
}