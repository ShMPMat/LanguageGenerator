package shmp.language.category.realization

import shmp.language.syntax.Clause
import shmp.language.CategoryRealization
import shmp.language.Word

class PrefixWordCategoryApplicator(prefixWord: Word) :
    WordCategoryApplicator(prefixWord, CategoryRealization.PrefixSeparateWord) {
    override fun apply(clause: Clause, wordPosition: Int): Clause =
        Clause(listOf(applicatorWord) + clause.words)

    override fun toString(): String {
        return "$applicatorWord, placed before the word"
    }

}