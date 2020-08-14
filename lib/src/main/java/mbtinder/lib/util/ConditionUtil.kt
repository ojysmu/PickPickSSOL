package mbtinder.lib.util

fun <T> ifValue(condition: Boolean, onTrue: T, onFalse: T) =
    if (condition) {
        onTrue
    } else {
        onFalse
    }
