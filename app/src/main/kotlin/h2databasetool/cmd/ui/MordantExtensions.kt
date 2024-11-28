package h2databasetool.cmd.ui

import com.github.ajalt.mordant.rendering.Widget
import com.github.ajalt.mordant.terminal.Terminal

inline fun renderOn(terminal: Terminal, buildWidget: Terminal.() -> Widget): Widget {
    val w = buildWidget(terminal)
    terminal.println(w)
    return w
}
