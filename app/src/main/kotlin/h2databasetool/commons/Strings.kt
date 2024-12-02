package h2databasetool.commons

fun String.stripMultiLine(): String = buildString(length) {
    this@stripMultiLine.lineSequence().forEach { line ->
        append(line.trimEnd())
    }
}

fun String.stripMultiLineToMargin(marginPrefix: String = "|"): String =
    trimMargin(marginPrefix).stripMultiLine()

fun StringBuilder.appendLine(string: Any?, vararg more: Any?): StringBuilder {
    append(string)
    more.onEach(::append)
    appendLine()
    return this
}

