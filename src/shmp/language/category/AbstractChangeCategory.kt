package shmp.language.category

import shmp.language.*

abstract class AbstractChangeCategory(
    final override val actualValues: List<CategoryValue>,
    final override val allPossibleValues: Set<CategoryValue>,
    override val outType: String,
    private val noCategoriesOut: String
) : Category {
    init {
        if (!allPossibleValues.containsAll(actualValues))
            throw LanguageException(
                "Nominal Category $outType was initialized with values which do not represent this category: "
                    + actualValues.filter { !allPossibleValues.contains(it) } .joinToString()
            )
    }

    override fun toString(): String {
        return outType + ":\n" + if (actualValues.isEmpty()) noCategoriesOut
        else actualValues.joinToString()
    }
}