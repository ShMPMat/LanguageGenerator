package shmp.language.categories

import shmp.language.*

abstract class AbstractChangeCategory(
    final override val categories: List<CategoryEnum>,
    final override val possibleCategories: Set<CategoryEnum>,
    override val outType: String,
    private val noCategoriesOut: String
) : Category {
    init {
        if (!possibleCategories.containsAll(categories))
            throw LanguageException(
                "Nominal Category $outType was initialized with values which do not represent this category: "
                    + categories.filter { !possibleCategories.contains(it) } .joinToString()
            )
    }

    override fun toString(): String {
        return outType + ":\n" + if (categories.isEmpty()) noCategoriesOut
        else categories.joinToString()
    }
}