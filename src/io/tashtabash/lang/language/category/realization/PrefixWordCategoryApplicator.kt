package io.tashtabash.lang.language.category.realization

import io.tashtabash.lang.language.category.realization.CategoryRealization.PrefixWord
import io.tashtabash.lang.language.category.paradigm.SourcedCategoryValue
import io.tashtabash.lang.language.lexis.Word
import io.tashtabash.lang.language.syntax.sequence.FoldedWordSequence
import io.tashtabash.lang.language.syntax.sequence.LatchType
import io.tashtabash.lang.language.syntax.sequence.LatchedWord


class PrefixWordCategoryApplicator(word: Word, latch: LatchType) : WordCategoryApplicator(word, latch, PrefixWord) {
    override fun apply(words: FoldedWordSequence, wordPosition: Int, values: Collection<SourcedCategoryValue>) =
        FoldedWordSequence(listOf(LatchedWord(word.copyWithValues(values), latch)) + words.words)

    override fun copy() = PrefixWordCategoryApplicator(word, latch)
    override fun copy(word: Word) = PrefixWordCategoryApplicator(word, latch)

    override fun toString() = "${word.getPhoneticRepresentation()}, placed before the word $latch"
}
