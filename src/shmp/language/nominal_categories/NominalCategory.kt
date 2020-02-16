package shmp.language.nominal_categories

import shmp.language.Clause
import shmp.language.NominalCategoryEnum
import shmp.language.NominalCategoryRealization
import shmp.language.Word

interface NominalCategory {
    fun apply(word: Word, nominalCategoryEnum: NominalCategoryEnum): Clause
}