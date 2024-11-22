package h2databasetool.utils

import com.github.ajalt.clikt.core.BaseCliktCommand
import com.github.ajalt.mordant.markdown.Markdown

fun BaseCliktCommand<*>.echoMarkdown(markdown: String) {
    echo(Markdown(markdown, hyperlinks = true))
}