package h2databasetool.utils

fun <C, T> C.add(item: T, vararg more: T) where C : MutableCollection<in T> {
    add(item)
    addAll(more)
}

inline fun <A, B, X, Y> Pair<A, B>.map(firsOf: (A) -> X, secondOf: (B) -> Y): Pair<X, Y> =
    firsOf(first) to secondOf(second)

inline fun <R : AutoCloseable, T> using(resource: R, block: R.() -> T): T = resource.use(block)

fun <T> Collection<T>.secondOrNull(): T? = iterator().run {
    next()
    if (hasNext()) next() else null
}

fun <T> Array<T>.second(): T = iterator().run { next(); next() }