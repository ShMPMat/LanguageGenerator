package shmp.lang.language.category

import shmp.lang.language.CategoryValue
import shmp.lang.language.LanguageException
import shmp.lang.language.lexis.SpeechPart


open class AbstractChangeCategory(
    final override val actualValues: List<CategoryValue>,
    final override val allPossibleValues: Set<CategoryValue>,
    final override val affected: Set<PSpeechPart>,
    final override val staticSpeechParts: Set<SpeechPart>,
    final override val outType: String
) : Category {
    override val speechParts = affected
        .map { it.speechPart }
        .toSet()

    init {
        if (!allPossibleValues.containsAll(actualValues))
            throw LanguageException(
                "Category $outType was initialized with values which do not represent this category: "
                        + actualValues.filter { !allPossibleValues.contains(it) }.joinToString()
            )
    }

    private val noCategoriesOut = "Has no $outType"

    override fun toString() = "$outType:\n" +
            if (actualValues.isEmpty())
                noCategoriesOut
            else actualValues.joinToString()
}