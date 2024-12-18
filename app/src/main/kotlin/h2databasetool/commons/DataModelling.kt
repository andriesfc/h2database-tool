@file:JvmName("DataModelling")

package h2databasetool.commons

import kotlin.random.Random

inline fun <T, A> Iterable<T>.mapWith(transform: T.() -> A) = map(transform)

fun <T> List<T>.randomItem() = this[Random.nextInt(0, size)]

fun <T> List<T>.copyOf(): List<T> = buildList(size) { addAll(this@copyOf) }
fun <T> Set<T>.copyOf(): Set<T> = toMutableSet().apply { addAll(this@copyOf) }.toSet()
fun <T> Collection<T>.copyOf(): Collection<T> =
    when (this) {
        is List<T> -> copyOf()
        is Set<T> -> copyOf()
        else -> buildList(size) { addAll(this@copyOf) }
    }

fun <A, B> pairOf(a: A, b: B): Pair<A, B> =
    Pair(a, b)

inline fun <A, B, T> Pair<A, B>.mapFirst(firstOf: (A) -> T): Pair<T, B> =
    let { (a, b) -> pairOf(firstOf(a), b) }

inline fun <A, B, T> Pair<A, B>.mapSecond(secondOf: (B) -> T): Pair<A, T> =
    let { (a, b) -> pairOf(a, secondOf(b)) }
