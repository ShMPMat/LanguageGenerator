package io.tashtabash.lang.language.category.realization

import io.tashtabash.lang.language.lexis.Word
import io.tashtabash.lang.language.syntax.sequence.LatchType


abstract class WordCategoryApplicator(
    val word: Word,
    val latch: LatchType,
    type: CategoryRealization
) : AbstractCategoryApplicator(type) {
    abstract fun copy(word: Word): WordCategoryApplicator
}
