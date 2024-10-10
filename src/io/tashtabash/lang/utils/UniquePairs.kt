package io.tashtabash.lang.utils


fun <T, U> composeUniquePairs(c1: Collection<T>, c2: Collection<U>): List<Pair<T, U>> =
    c1.flatMapIndexed { i, x ->
        c2.drop(i)
            .map { y -> x to y }
    }
