package io.tashtabash.lang.language.category.realization

import io.tashtabash.lang.language.category.realization.CategoryRealization.*


enum class CategoryRealization {
    Suppletion,
    Prefix,
    Suffix,
    PrefixWord,
    SuffixWord,
    Reduplication,
    Passing
}


val categoryRealizationClusters = listOf(
    listOf(Prefix, Suffix),
    listOf(PrefixWord, SuffixWord)
)

val analyticalRealizations = listOf(Passing, PrefixWord, SuffixWord)
