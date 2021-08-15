package shmp.lang.language.category

import shmp.lang.language.category.value.CategoryValue
import shmp.lang.language.category.value.CategoryValues
import shmp.lang.language.LanguageException
import shmp.lang.language.lexis.SpeechPart
import shmp.lang.utils.notEqualsByElement


open class AbstractChangeCategory(
    final override val actualValues: CategoryValues,
    final override val allPossibleValues: Set<CategoryValue>,
    final override val affected: Set<PSpeechPart>,
    final override val staticSpeechParts: Set<SpeechPart>,
    final override val outType: String
) : Category {
    override val speechParts = affected
        .map { it.speechPart }
        .toSet()

    init {
        actualValues.filter { !allPossibleValues.contains(it) }
            .takeIf { it.isNotEmpty() }
            ?.let { vs ->
                throw LanguageException(
                    "Category $outType was initialized with values which do not represent this category: "
                            + vs.joinToString { it.parentClassName + " " + it.alias }
                )
            }
    }

    private val noCategoriesOut = "Has no $outType"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AbstractChangeCategory

        if (outType != other.outType) return false
        if (actualValues notEqualsByElement other.actualValues) return false

        return true
    }

    override fun hashCode(): Int {
        var result = actualValues.hashCode()
        result = 31 * result + outType.hashCode()
        return result
    }

    override fun toString() = "$outType:\n" +
            if (actualValues.isEmpty())
                noCategoriesOut
            else actualValues.joinToString()
}
