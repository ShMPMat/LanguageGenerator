package io.tashtabash.lang.language.category.realization

import io.tashtabash.lang.language.category.realization.CategoryRealization.Suppletion
import io.tashtabash.lang.language.category.paradigm.SourcedCategoryValue
import io.tashtabash.lang.language.lexis.Word
import io.tashtabash.lang.language.syntax.sequence.FoldedWordSequence
import io.tashtabash.lang.language.syntax.sequence.LatchType


class SuppletionCategoryApplicator(word: Word) : WordCategoryApplicator(word, LatchType.Center, Suppletion) {
    override fun apply(words: FoldedWordSequence, wordPosition: Int, values: Collection<SourcedCategoryValue>) =
        words.swapWord(wordPosition) { w -> word.copyAndAddValues(w.categoryValues + values) }

    override fun copy() = SuppletionCategoryApplicator(word)
    override fun copy(word: Word) = SuppletionCategoryApplicator(word)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return word == (other as SuppletionCategoryApplicator).word
    }

    override fun hashCode(): Int {
        return word.hashCode()
    }

    override fun toString() = "Suppletion: $word"
}
