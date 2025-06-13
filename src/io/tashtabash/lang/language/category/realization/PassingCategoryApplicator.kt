package io.tashtabash.lang.language.category.realization

import io.tashtabash.lang.language.category.paradigm.SourcedCategoryValue
import io.tashtabash.lang.language.syntax.sequence.FoldedWordSequence


object PassingCategoryApplicator : AbstractCategoryApplicator(CategoryRealization.Passing) {
    override fun apply(words: FoldedWordSequence, wordPosition: Int, values: Collection<SourcedCategoryValue>) =
        words.swapWord(wordPosition) {
            val newCategoryValues = it.categoryValues + values
            // Inject the values in the roots instead of creating a mock Ø morpheme
            val newMorphemes = it.morphemes.map { morpheme ->
                if (morpheme.isRoot)
                    // Filter the original values and not vice versa, there's a good chance there are none
                    morpheme.copy(categoryValues = values + morpheme.categoryValues.filter { v -> v !in values } )
                else
                    morpheme
            }

            it.copy(categoryValues = newCategoryValues, morphemes = newMorphemes)
        }

    override fun copy() = PassingCategoryApplicator

    override fun toString() = "Nothing"
}
