package h2databasetool.commons

fun String.line(): String = buildString(length) {
    this@line.lineSequence().forEach { line ->
        append(line.trimEnd())
    }
}