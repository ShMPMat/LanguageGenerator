package shmp.lang.language.category.realization

import shmp.lang.language.syntax.WordSequence
import shmp.lang.language.CategoryRealization
import shmp.lang.language.lexis.Word
import shmp.lang.language.category.paradigm.ParametrizedCategoryValue

class SuffixWordCategoryApplicator(suffixWord: Word) :
    WordCategoryApplicator(suffixWord, CategoryRealization.SuffixSeparateWord) {
    override fun apply(wordSequence: WordSequence, wordPosition: Int, values: Collection<ParametrizedCategoryValue>): WordSequence =
        WordSequence(wordSequence.words + listOf(applicatorWord.copyWithValues(values)))

    override fun toString(): String {
        return "$applicatorWord, placed after the word"
    }
}