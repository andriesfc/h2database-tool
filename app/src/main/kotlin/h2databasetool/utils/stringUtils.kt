package h2databasetool.utils

inline fun String.splitAround(delimiter: String, ifNotAfter: (String) -> String?): Pair<String, String?> {
    return when (val pos = indexOf(delimiter)) {
        -1 -> this to ifNotAfter(this)
        else -> substring(0, pos) to substring(pos + 1)
    }
}