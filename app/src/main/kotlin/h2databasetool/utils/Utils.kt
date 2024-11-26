package h2databasetool.utils

fun <C, T> C.add(item: T, vararg more: T) where C : MutableCollection<in T> {
    add(item)
    addAll(more)
}

inline fun <A, B, X, Y> Pair<A, B>.map(firsOf: (A) -> X, secondOf: (B) -> Y): Pair<X, Y> =
    firsOf(first) to secondOf(second)

inline fun <A, B, X> Pair<A, B>.mapFirst(firstOf: (A) -> X): Pair<X, B> = map(firstOf, { it })

inline fun <A, B, Y> Pair<A, B>.mapSecond(secondOf: (B) -> Y): Pair<A, Y> = map({ it }, secondOf)

inline fun <R : AutoCloseable, T> using(resource: R, block: R.() -> T): T = resource.use(block)

inline fun <T> String.withBeforeAndAfter(
    delimiter: String, process: (before: String, after: String?, index: Int) -> T
): T = when (val index = indexOf(delimiter)) {
    -1 -> process(this, null, index)
    else -> process(substring(0, index), substring(index + delimiter.length), index)
}

fun <T> Collection<T>.second(): T = iterator().run { next(); next() }

