package io.tashtabash.lang.utils


fun <E> List<List<E>>.joinToList(
    separator: List<E> = listOf(),
    prefix: List<E> = listOf(),
    postfix: List<E> = listOf()
): List<E> {
    val result = prefix.toMutableList()

    val iterator = iterator()
    while (iterator.hasNext()) {
        result += iterator.next()

        if (iterator.hasNext())
            result += separator
    }

    result += postfix

    return result
}
