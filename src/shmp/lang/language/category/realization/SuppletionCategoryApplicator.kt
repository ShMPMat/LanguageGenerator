package shmp.lang.language.category.realization

import shmp.lang.language.CategoryRealization.Suppletion
import shmp.lang.language.category.paradigm.SourcedCategoryValue
import shmp.lang.language.lexis.Word
import shmp.lang.language.syntax.WordSequence


class SuppletionCategoryApplicator(word: Word) : WordCategoryApplicator(word, Suppletion) {
    override fun apply(words: WordSequence, wordPosition: Int, values: Collection<SourcedCategoryValue>) =
        words.swapWord(wordPosition) { w -> word.copyAndAddValues(w.categoryValues + values) }

    override fun copy() = SuppletionCategoryApplicator(word)

    override fun toString() = "Suppletion: $word"
}
