package io.tashtabash.lang.language.category.realization

import io.tashtabash.lang.language.category.realization.CategoryRealization.PrefixWord
import io.tashtabash.lang.language.category.paradigm.SourcedCategoryValue
import io.tashtabash.lang.language.lexis.Word
import io.tashtabash.lang.language.syntax.sequence.FoldedWordSequence
import io.tashtabash.lang.language.syntax.sequence.LatchType


class PrefixWordCategoryApplicator(word: Word, latch: LatchType) : WordCategoryApplicator(word, latch, PrefixWord) {
    override fun apply(words: FoldedWordSequence, wordPosition: Int, values: Collection<SourcedCategoryValue>) =
        FoldedWordSequence(listOf(word.copyWithValues(values) to latch) + words.words)

    override fun copy() = PrefixWordCategoryApplicator(word, latch)

    override fun toString() = "$word, placed before the word $latch"
}
