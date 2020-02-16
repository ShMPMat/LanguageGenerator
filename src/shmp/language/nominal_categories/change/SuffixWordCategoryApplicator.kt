package shmp.language.nominal_categories.change

import shmp.language.Clause
import shmp.language.NominalCategoryRealization
import shmp.language.Word

class SuffixWordCategoryApplicator(suffixWord: Word) :
    WordCategoryApplicator(suffixWord, NominalCategoryRealization.SuffixSeparateWord) {
    override fun apply(word: Word) = Clause(listOf(applicatorWord, word))

    override fun toString(): String {
        return "$applicatorWord, placed after the word"
    }
}