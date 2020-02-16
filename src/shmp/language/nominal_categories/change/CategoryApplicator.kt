package shmp.language.nominal_categories.change

import shmp.language.Clause
import shmp.language.Word

interface CategoryApplicator {
    fun apply(word: Word): Clause
}