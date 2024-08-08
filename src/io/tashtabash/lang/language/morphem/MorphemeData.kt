package io.tashtabash.lang.language.morphem

import io.tashtabash.lang.language.category.paradigm.SourcedCategoryValue


data class MorphemeData(
    val size: Int,
    val categoryValues: List<SourcedCategoryValue>,
    val isRoot: Boolean
)
