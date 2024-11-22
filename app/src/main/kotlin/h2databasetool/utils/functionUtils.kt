package h2databasetool.utils

inline fun <A, B, X, Y> Pair<A, B>.map(firsOf: (A) -> X, secondOf: (B) -> Y): Pair<X, Y> = firsOf(first) to secondOf(second)
inline fun <A, B, X> Pair<A, B>.mapFirst(firstOf: (A) -> X): Pair<X, B> = map(firstOf, { it })
inline fun <A, B, Y> Pair<A, B>.mapSecond(secondOf: (B) -> Y): Pair<A, Y> = map({ it }, secondOf)
