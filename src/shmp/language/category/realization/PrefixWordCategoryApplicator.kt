package shmp.language.category.realization

import shmp.language.syntax.WordSequence
import shmp.language.CategoryRealization
import shmp.language.lexis.Word
import shmp.language.category.paradigm.ParametrizedCategoryValue

class PrefixWordCategoryApplicator(prefixWord: Word) :
    WordCategoryApplicator(prefixWord, CategoryRealization.PrefixSeparateWord) {
    override fun apply(wordSequence: WordSequence, wordPosition: Int, values: Collection<ParametrizedCategoryValue>): WordSequence =
        WordSequence(listOf(applicatorWord.copyWithValues(values)) + wordSequence.words)

    override fun toString(): String {
        return "$applicatorWord, placed before the word"
    }

}