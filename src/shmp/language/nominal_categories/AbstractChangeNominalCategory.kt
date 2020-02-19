package shmp.language.nominal_categories

import shmp.language.*
import shmp.language.nominal_categories.change.CategoryApplicator

abstract class AbstractChangeNominalCategory(
    final override val categories: Set<NominalCategoryEnum>,
    final override val possibleCategories: Set<NominalCategoryEnum>,
    private val outType: String,
    private val noCategoriesOut: String
) : NominalCategory {
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