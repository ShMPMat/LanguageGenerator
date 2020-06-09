package shmp.language.category.realization

import shmp.language.syntax.Clause
import shmp.language.CategoryRealization
import shmp.language.lexis.Word
import shmp.language.category.paradigm.ParametrizedCategoryValue

class SuffixWordCategoryApplicator(suffixWord: Word) :
    WordCategoryApplicator(suffixWord, CategoryRealization.SuffixSeparateWord) {
    override fun apply(clause: Clause, wordPosition: Int, values: Collection<ParametrizedCategoryValue>): Clause =
        Clause(clause.words + listOf(applicatorWord.copyWithValues(values)))

    override fun toString(): String {
        return "$applicatorWord, placed after the word"
    }
}