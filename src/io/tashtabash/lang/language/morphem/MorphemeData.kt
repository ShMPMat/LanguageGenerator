package io.tashtabash.lang.language.morphem

import io.tashtabash.lang.language.category.paradigm.SourcedCategoryValue
import io.tashtabash.lang.language.derivation.DerivationClass


data class MorphemeData(
    val size: Int,
    val categoryValues: List<SourcedCategoryValue>,
    val isRoot: Boolean,
    val derivationValues: List<DerivationClass> = listOf()
)
