package h2databasetool.cmd.ui

import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextColors.Companion.rgb
import com.github.ajalt.mordant.rendering.TextStyles
import com.github.ajalt.mordant.rendering.TextStyles.*

/**
 * A collection of styling functions for use with a full color ANSI
 * terminal.
 */
data object Style {

    data object Colors {
        val illuminatingEmerald = rgb("#359570ff")
        val chartreuse = rgb("#b0fc38")
        val cerulean = rgb("#0492C2")
    }

    val boldEmphasis = bold
    val notice = Colors.illuminatingEmerald + bold
    val softFocus = TextStyles.dim
    val softFocusError = softFocus + TextColors.yellow + italic
    val softYellowFocus = softFocus + TextColors.brightYellow

    val h1 = (underline + Colors.cerulean + bold).let { styledText ->
        { s: String -> styledText(s.uppercase()) }
    }

    val h2 = fun(heading: String) = (Colors.chartreuse + bold)(heading)

    val info1 = fun(s: String) = (Colors.illuminatingEmerald + italic)(s)

}

