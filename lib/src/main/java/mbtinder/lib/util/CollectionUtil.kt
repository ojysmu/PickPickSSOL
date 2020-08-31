package mbtinder.lib.util

fun <T> List<T>.merge(list: List<T>): List<T> {
    return toMutableList().apply { addAll(list) }
}