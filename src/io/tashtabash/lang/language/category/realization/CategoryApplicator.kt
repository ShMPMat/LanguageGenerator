package io.tashtabash.lang.language.category.realization

import io.tashtabash.lang.language.category.paradigm.SourcedCategoryValue
import io.tashtabash.lang.language.syntax.sequence.FoldedWordSequence


interface CategoryApplicator {
    val type: CategoryRealization?

    fun apply(words: FoldedWordSequence, wordPosition: Int, values: Collection<SourcedCategoryValue>): FoldedWordSequence

    fun copy(): CategoryApplicator
}
