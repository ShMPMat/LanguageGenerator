package shmp.language.category.realization

import shmp.language.syntax.WordSequence
import shmp.language.CategoryRealization
import shmp.language.lexis.Word
import shmp.language.category.paradigm.ParametrizedCategoryValue

class SuffixWordCategoryApplicator(suffixWord: Word) :
    WordCategoryApplicator(suffixWord, CategoryRealization.SuffixSeparateWord) {
    override fun apply(wordSequence: WordSequence, wordPosition: Int, values: Collection<ParametrizedCategoryValue>): WordSequence =
        WordSequence(wordSequence.words + listOf(applicatorWord.copyWithValues(values)))

    override fun toString(): String {
        return "$applicatorWord, placed after the word"
    }
}