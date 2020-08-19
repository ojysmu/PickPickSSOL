package mbtinder.lib.util

fun <T> Collection<T>.findAll(predicate: (T) -> Boolean) = filterTo(ArrayList(), predicate)

fun <B, D: B> Collection<D>.mapBase(transform: (D) -> B = { it }): ArrayList<B> {
    val asList = arrayListOf<B>()
    for (element in this) {
        asList.add(transform.invoke(element))
    }
    return asList
}

fun <T> Collection<T>.merge(collection: Collection<T>): Collection<T> {
    return toMutableList().apply { addAll(collection) }
}

fun <T> List<T>.merge(list: List<T>): List<T> {
    return toMutableList().apply { addAll(list) }
}