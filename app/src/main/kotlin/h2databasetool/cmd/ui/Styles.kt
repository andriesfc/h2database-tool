package h2databasetool.cmd.ui

import com.github.ajalt.mordant.rendering.TextColors.Companion.rgb
import com.github.ajalt.mordant.rendering.TextStyles
import com.github.ajalt.mordant.rendering.TextStyles.bold

data object Styles {

    data object Colors {
        val illuminatingEmerald = rgb("#359570FF")
    }

    val boldEmphasis = bold
    val notice = Colors.illuminatingEmerald
    val softFocus = TextStyles.dim
}