package h2databasetool.commons

fun String.stripLineFeeds(): String = buildString(length) {
    this@stripLineFeeds.lineSequence().forEach { line ->
        append(line.trimEnd())
    }
}

fun String.stripLineFeedsToMargin(marginPrefix: String = "|"): String =
    trimMargin(marginPrefix).stripLineFeeds()

fun flattenStr(s: String, trimEnd: Boolean = false): String {
    if (s.isEmpty()) return s
    val iter = s.lineSequence().dropWhile { line -> line.isEmpty() || line.isBlank() }.iterator()
    var line = if (iter.hasNext()) iter.next() else return ""
    val leftMargin = when (val i = line.indexOf('|')) {
        -1 -> line.takeWhile { it.isWhitespace() }.count()
        else -> i + 1 // Always start after left margin character

    }
    return buildString(s.length) {
        while (true) {
            if (trimEnd)
                line = line.trimEnd()
            if (leftMargin < line.length)
                append(line, leftMargin, line.length)
            line = if (iter.hasNext()) iter.next() else break
        }
    }
}

fun String.flatten(trimEnd: Boolean = false) = flattenStr(this, trimEnd)
