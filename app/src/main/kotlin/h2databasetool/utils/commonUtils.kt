package h2databasetool.utils

fun <C,T> C.add(item: T, vararg more: T) where C: MutableCollection<in T>{
    add(item)
    addAll(more)
}