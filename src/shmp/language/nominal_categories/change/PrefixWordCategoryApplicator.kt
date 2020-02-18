package shmp.language.nominal_categories.change

import shmp.language.Clause
import shmp.language.NominalCategoryRealization
import shmp.language.Word

class PrefixWordCategoryApplicator(prefixWord: Word) :
    WordCategoryApplicator(prefixWord, NominalCategoryRealization.PrefixSeparateWord) {
    override fun apply(clause: Clause, wordPosition: Int): Clause = Clause(listOf(applicatorWord) + clause.words)

    override fun toString(): String {
        return "$applicatorWord, placed before the word"
    }

}