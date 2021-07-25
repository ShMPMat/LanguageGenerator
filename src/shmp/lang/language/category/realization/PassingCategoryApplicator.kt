package shmp.lang.language.category.realization

import shmp.lang.language.category.paradigm.SourcedCategoryValue
import shmp.lang.language.syntax.WordSequence


object PassingCategoryApplicator : CategoryApplicator {
    override fun apply(words: WordSequence, wordPosition: Int, values: Collection<SourcedCategoryValue>) =
        words.swapWord(wordPosition) { it.copyAndAddValues(values) }

    override fun copy() = PassingCategoryApplicator

    override fun toString() = "Nothing"
}
