package h2databasetool.commons

fun String.stripLineFeeds(): String = buildString(length) {
    this@stripLineFeeds.lineSequence().forEach { line ->
        append(line.trimEnd())
    }
}

fun String.stripLineFeedsToMargin(marginPrefix: String = "|"): String =
    trimMargin(marginPrefix).stripLineFeeds()

fun StringBuilder.appendLine(string: Any?, vararg more: Any?): StringBuilder {
    append(string)
    more.onEach(::append)
    appendLine()
    return this
}

