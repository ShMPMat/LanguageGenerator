package shmp.language.category.realization

import shmp.language.syntax.Clause
import shmp.language.CategoryRealization
import shmp.language.Word
import shmp.language.category.paradigm.ParametrizedCategoryValue

class PrefixWordCategoryApplicator(prefixWord: Word) :
    WordCategoryApplicator(prefixWord, CategoryRealization.PrefixSeparateWord) {
    override fun apply(clause: Clause, wordPosition: Int, values: Collection<ParametrizedCategoryValue>): Clause =
        Clause(listOf(applicatorWord.copyWithValues(values)) + clause.words)

    override fun toString(): String {
        return "$applicatorWord, placed before the word"
    }

}