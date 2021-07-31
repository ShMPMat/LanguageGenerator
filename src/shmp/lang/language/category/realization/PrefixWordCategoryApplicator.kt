package shmp.lang.language.category.realization

import shmp.lang.language.CategoryRealization.PrefixWord
import shmp.lang.language.category.paradigm.SourcedCategoryValue
import shmp.lang.language.lexis.Word
import shmp.lang.language.syntax.sequence.FoldedWordSequence
import shmp.lang.language.syntax.sequence.LatchType


class PrefixWordCategoryApplicator(word: Word, latch: LatchType) : WordCategoryApplicator(word, latch, PrefixWord) {
    override fun apply(words: FoldedWordSequence, wordPosition: Int, values: Collection<SourcedCategoryValue>) =
        FoldedWordSequence(listOf(word.copyWithValues(values) to latch) + words.words)

    override fun copy() = PrefixWordCategoryApplicator(word, latch)

    override fun toString() = "$word, placed before the word $latch"
}
