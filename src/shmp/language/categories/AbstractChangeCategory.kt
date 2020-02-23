package shmp.language.categories

import shmp.language.*

abstract class AbstractChangeCategory(
    final override val values: List<CategoryValue>,
    final override val possibleValues: Set<CategoryValue>,
    override val outType: String,
    private val noCategoriesOut: String
) : Category {
    init {
        if (!possibleValues.containsAll(values))
            throw LanguageException(
                "Nominal Category $outType was initialized with values which do not represent this category: "
                    + values.filter { !possibleValues.contains(it) } .joinToString()
            )
    }

    override fun toString(): String {
        return outType + ":\n" + if (values.isEmpty()) noCategoriesOut
        else values.joinToString()
    }
}