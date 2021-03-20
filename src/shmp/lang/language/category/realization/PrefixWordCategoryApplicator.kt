package shmp.lang.language.category.realization

import shmp.lang.language.syntax.WordSequence
import shmp.lang.language.CategoryRealization
import shmp.lang.language.lexis.Word
import shmp.lang.language.category.paradigm.ParametrizedCategoryValue

class PrefixWordCategoryApplicator(prefixWord: Word) :
    WordCategoryApplicator(prefixWord, CategoryRealization.PrefixSeparateWord) {
    override fun apply(wordSequence: WordSequence, wordPosition: Int, values: Collection<ParametrizedCategoryValue>): WordSequence =
        WordSequence(listOf(applicatorWord.copyWithValues(values)) + wordSequence.words)

    override fun toString(): String {
        return "$applicatorWord, placed before the word"
    }

}