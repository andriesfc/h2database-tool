@file:JvmName("DataModelling")

package h2databasetool.commons

import kotlin.random.Random

inline fun <T, A> Iterable<T>.mapWith(transform: T.() -> A) = map(transform)

fun <T> List<T>.randomItem() = this[Random.nextInt(0, size)]
