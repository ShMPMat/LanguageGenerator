package shmp.lang.language.category.realization

import shmp.lang.language.CategoryRealization
import shmp.lang.language.category.paradigm.SourcedCategoryValue
import shmp.lang.language.syntax.sequence.FoldedWordSequence


object PassingCategoryApplicator : AbstractCategoryApplicator(CategoryRealization.Passing) {
    override fun apply(words: FoldedWordSequence, wordPosition: Int, values: Collection<SourcedCategoryValue>) =
        words.swapWord(wordPosition) { it.copyAndAddValues(values) }

    override fun copy() = PassingCategoryApplicator

    override fun toString() = "Nothing"
}
