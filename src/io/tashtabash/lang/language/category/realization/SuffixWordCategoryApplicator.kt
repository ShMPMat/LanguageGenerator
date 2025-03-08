package io.tashtabash.lang.language.category.realization

import io.tashtabash.lang.language.category.realization.CategoryRealization.SuffixWord
import io.tashtabash.lang.language.category.paradigm.SourcedCategoryValue
import io.tashtabash.lang.language.lexis.Word
import io.tashtabash.lang.language.syntax.sequence.FoldedWordSequence
import io.tashtabash.lang.language.syntax.sequence.LatchType
import io.tashtabash.lang.language.syntax.sequence.LatchedWord


class SuffixWordCategoryApplicator(word: Word, latch: LatchType) : WordCategoryApplicator(word, latch, SuffixWord) {
    override fun apply(words: FoldedWordSequence, wordPosition: Int, values: Collection<SourcedCategoryValue>) =
        FoldedWordSequence(words.words + LatchedWord(word.copyWithValues(values), latch))

    override fun copy() = SuffixWordCategoryApplicator(word, latch)
    override fun copy(word: Word) = SuffixWordCategoryApplicator(word, latch)

    override fun toString() = "${word.getPhoneticRepresentation()}, placed after the word $latch"
}
