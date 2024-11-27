package h2databasetool.cmd.ui

import com.github.ajalt.mordant.rendering.Widget
import com.github.ajalt.mordant.terminal.Terminal

inline fun Terminal.render(buildWidget: Terminal.() -> Widget): Widget {
    val w = buildWidget(this)
    println(w)
    return w
}
