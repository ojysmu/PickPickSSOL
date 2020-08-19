package mbtinder.lib.util

fun Pair<Int, Int>.sum(): Int {
    return first + second
}

fun Triple<Int, Int, Int>.sum(): Int {
    return first + second + third
}