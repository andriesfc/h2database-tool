package h2databasetool.cmd.option

import com.github.ajalt.clikt.parameters.options.RawOption
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.help

fun RawOption.quiet() =
    help("Do not output user messages to the console.").flag(defaultForHelp = "false")
