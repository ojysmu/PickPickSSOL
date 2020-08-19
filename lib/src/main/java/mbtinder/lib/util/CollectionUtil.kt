package mbtinder.lib.util

fun <T> Collection<T>.findAll(predicate: (T) -> Boolean) = filterTo(ArrayList(), predicate)

fun <B, D: B> Collection<D>.mapBase(transform: (D) -> B = { it }): ArrayList<B> {
    val asList = arrayListOf<B>()
    for (element in this) {
        asList.add(transform.invoke(element))
    }
    return asList
}