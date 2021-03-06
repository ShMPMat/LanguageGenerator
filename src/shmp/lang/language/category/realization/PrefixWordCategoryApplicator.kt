package shmp.lang.language.category.realization

import shmp.lang.language.syntax.WordSequence
import shmp.lang.language.CategoryRealization
import shmp.lang.language.lexis.Word
import shmp.lang.language.category.paradigm.SourcedCategoryValue


class PrefixWordCategoryApplicator(word: Word) : WordCategoryApplicator(word, CategoryRealization.PrefixSeparateWord) {
    override fun apply(words: WordSequence, wordPosition: Int, values: Collection<SourcedCategoryValue>) =
        WordSequence(listOf(word.copyWithValues(values)) + words.words)

    override fun copy() = PrefixWordCategoryApplicator(word)

    override fun toString() = "$word, placed before the word"
}
