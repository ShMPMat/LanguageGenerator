package shmp.lang.language.category.realization

import shmp.lang.language.CategoryRealization.Suppletion
import shmp.lang.language.category.paradigm.SourcedCategoryValue
import shmp.lang.language.lexis.Word
import shmp.lang.language.syntax.sequence.FoldedWordSequence
import shmp.lang.language.syntax.sequence.LatchType


class SuppletionCategoryApplicator(word: Word) : WordCategoryApplicator(word, LatchType.Center, Suppletion) {
    override fun apply(words: FoldedWordSequence, wordPosition: Int, values: Collection<SourcedCategoryValue>) =
        words.swapWord(wordPosition) { w -> word.copyAndAddValues(w.categoryValues + values) }

    override fun copy() = SuppletionCategoryApplicator(word)

    override fun toString() = "Suppletion: $word"
}
