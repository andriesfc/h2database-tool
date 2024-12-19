package h2databasetool.commons

import com.github.ajalt.mordant.rendering.Widget
import com.github.ajalt.mordant.terminal.Terminal

inline fun render(terminal: Terminal, buildWidget: Terminal.() -> Widget) {
    val w = buildWidget(terminal)
    terminal.println(w)
}
