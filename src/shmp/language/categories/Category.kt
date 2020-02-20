package shmp.language.categories

import shmp.language.NominalCategoryEnum

interface Category {
    val categories: List<NominalCategoryEnum>
    val possibleCategories: Set<NominalCategoryEnum>
}