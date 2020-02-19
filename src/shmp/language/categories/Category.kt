package shmp.language.categories

import shmp.language.NominalCategoryEnum

interface Category {
    val categories: Set<NominalCategoryEnum>
    val possibleCategories: Set<NominalCategoryEnum>
}