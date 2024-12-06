package h2databasetool.commons.terminal

import com.github.ajalt.clikt.core.BaseCliktCommand
import com.github.ajalt.clikt.core.PrintMessage
import com.github.ajalt.mordant.markdown.Markdown

const val NL = '\u0085'

fun BaseCliktCommand<*>.echoMarkdown(markdown: String) {
    echo(Markdown(markdown, hyperlinks = true))
}

fun BaseCliktCommand<*>.fail(message: String, exitCode: Int = 1) : Nothing =
    throw PrintMessage(message, exitCode)


fun line(s: String) = s.trimEnd() + NL
fun String.nl() = line(this)

fun StringBuilder.line(str: String): java.lang.StringBuilder = append(str.nl())
fun StringBuilder.line() = append(NL)
