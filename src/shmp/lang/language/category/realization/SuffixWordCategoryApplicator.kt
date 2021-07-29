package shmp.lang.language.category.realization

import shmp.lang.language.CategoryRealization.SuffixWord
import shmp.lang.language.category.paradigm.SourcedCategoryValue
import shmp.lang.language.lexis.Word
import shmp.lang.language.syntax.WordSequence


class SuffixWordCategoryApplicator(word: Word) : WordCategoryApplicator(word, SuffixWord) {
    override fun apply(words: WordSequence, wordPosition: Int, values: Collection<SourcedCategoryValue>) =
        WordSequence(words.words + word.copyWithValues(values))

    override fun copy() = SuffixWordCategoryApplicator(word)

    override fun toString() = "$word, placed after the word"
}
