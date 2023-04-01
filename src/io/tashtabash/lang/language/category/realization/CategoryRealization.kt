package io.tashtabash.lang.language.category.realization


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
    listOf(CategoryRealization.Prefix, CategoryRealization.Suffix),
    listOf(CategoryRealization.PrefixWord, CategoryRealization.SuffixWord)
)
