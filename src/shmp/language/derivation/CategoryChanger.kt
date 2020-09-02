package shmp.language.derivation

import shmp.language.CategoryValue
import shmp.language.SpeechPart
import shmp.language.lexis.SemanticsCore


interface CategoryChanger {
    fun makeStaticCategories(semanticsCores: List<SemanticsCore>, speechPart: SpeechPart): Set<CategoryValue>?

    val defaultToString: String
}

abstract class AbstractCategoryChanger : CategoryChanger {
    override fun toString() = defaultToString
}


class PassingCategoryChanger(private val index: Int) : AbstractCategoryChanger() {
    override fun makeStaticCategories(semanticsCores: List<SemanticsCore>, speechPart: SpeechPart) =
        semanticsCores.getOrNull(index)?.let {
            if (it.speechPart == speechPart)
                it.staticCategories
            else null
        }

    override val defaultToString = "Same categories which a parent have"
}

class ConstantCategoryChanger(
    val categories: Set<CategoryValue>,
    val targetSpeechPart: SpeechPart
) : AbstractCategoryChanger() {
    override fun makeStaticCategories(semanticsCores: List<SemanticsCore>, speechPart: SpeechPart) =
        if (targetSpeechPart == speechPart) categories
        else null

    override val defaultToString =
        if (categories.isEmpty()) "Makes no categories"
        else "Always makes word " + categories.joinToString()
}
