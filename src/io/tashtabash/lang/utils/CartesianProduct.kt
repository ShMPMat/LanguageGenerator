package io.tashtabash.lang.utils


fun <T> listCartesianProduct(l: List<Collection<T>>): List<List<T>> {
    if (l.isEmpty())
        return listOf(emptyList())

    var result = l[0].map { listOf(it) }

    for (cl in l.drop(1)) {
        result = cartesianProduct(result, cl)
            .map { (list, elem) -> listOf(elem) + list }
            .toMutableList()
    }

    return result
}

fun <T> List<Collection<T>>.cartesianProduct() = listCartesianProduct(this)

fun <T, U> cartesianProduct(c1: Collection<T>, c2: Collection<U>): List<Pair<T, U>> =
    c1.flatMap { x ->
        c2.map { y -> x to y }
    }
