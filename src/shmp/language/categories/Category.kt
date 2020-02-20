package shmp.language.categories

import shmp.language.CategoryEnum

interface Category {
    val categories: List<CategoryEnum>
    val possibleCategories: Set<CategoryEnum>
}