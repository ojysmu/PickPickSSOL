package mbtinder.lib.util

fun <B, D: B> Collection<D>.mapBase(transform: (D) -> B = { it }): ArrayList<B> {
    val asList = arrayListOf<B>()
    for (element in this) {
        asList.add(transform.invoke(element))
    }
    return asList
}

fun <T> List<T>.merge(list: List<T>): List<T> {
    return toMutableList().apply { addAll(list) }
}