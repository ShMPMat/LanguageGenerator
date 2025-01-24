package io.tashtabash.lang.language.category.realization

import io.tashtabash.lang.language.category.paradigm.SourcedCategoryValue
import io.tashtabash.lang.language.morphem.MorphemeData
import io.tashtabash.lang.language.syntax.sequence.FoldedWordSequence


object PassingCategoryApplicator : AbstractCategoryApplicator(CategoryRealization.Passing) {
    override fun apply(words: FoldedWordSequence, wordPosition: Int, values: Collection<SourcedCategoryValue>) =
        words.swapWord(wordPosition) {
            val newCategoryValues = it.categoryValues + values
            val newMorphemes = it.morphemes + MorphemeData(0, values.toList())

            it.copy(categoryValues = newCategoryValues, morphemes = newMorphemes)
        }

    override fun copy() = PassingCategoryApplicator

    override fun toString() = "Nothing"
}
