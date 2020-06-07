package shmp.language.category

import shmp.language.*

abstract class AbstractChangeCategory(
    final override val actualValues: List<CategoryValue>,
    final override val allPossibleValues: Set<CategoryValue>,
    final override val affected: Set<ParametrizedSpeechPart>,
    final override val outType: String
) : Category {
    override val speechParts = affected
        .map { it.first }
        .toSet()

    init {
        if (!allPossibleValues.containsAll(actualValues))
            throw LanguageException(
                "Nominal Category $outType was initialized with values which do not represent this category: "
                        + actualValues.filter { !allPossibleValues.contains(it) }.joinToString()
            )
    }

    private val noCategoriesOut: String = "Has no $outType"

    override fun toString(): String {
        return "$outType:\n" +
                if (actualValues.isEmpty())
                    noCategoriesOut
                else actualValues.joinToString()
    }
}