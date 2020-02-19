package shmp.language.nominal_categories

import shmp.language.Clause
import shmp.language.NominalCategoryEnum
import shmp.language.NominalCategoryRealization
import shmp.language.Word

interface NominalCategory {
    val categories: Set<NominalCategoryEnum>
    val possibleCategories: Set<NominalCategoryEnum>

    fun apply(clause: Clause, wordPosition: Int, nominalCategoryEnum: NominalCategoryEnum): Clause
}