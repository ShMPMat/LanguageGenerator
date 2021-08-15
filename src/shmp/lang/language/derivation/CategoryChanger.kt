package shmp.lang.language.derivation

import shmp.lang.language.category.value.CategoryValue
import shmp.lang.language.lexis.SemanticsCore
import shmp.lang.language.lexis.TypedSpeechPart


interface CategoryChanger {
    fun makeStaticCategories(semanticsCores: List<SemanticsCore>, speechPart: TypedSpeechPart): Set<CategoryValue>?

    val defaultToString: String
}

abstract class AbstractCategoryChanger : CategoryChanger {
    override fun toString() = defaultToString
}


class PassingCategoryChanger(private val index: Int) : AbstractCategoryChanger() {
    override fun makeStaticCategories(semanticsCores: List<SemanticsCore>, speechPart: TypedSpeechPart) =
        semanticsCores.getOrNull(index)?.let {
            if (it.speechPart == speechPart)
                it.staticCategories
            else null
        }

    override val defaultToString = "Same categories which a parent have"
}

class ConstantCategoryChanger(
    val categories: Set<CategoryValue>,
    val targetSpeechPart: TypedSpeechPart
) : AbstractCategoryChanger() {
    override fun makeStaticCategories(semanticsCores: List<SemanticsCore>, speechPart: TypedSpeechPart) =
        if (targetSpeechPart == speechPart) categories
        else null

    override val defaultToString =
        if (categories.isEmpty()) "Makes no categories"
        else "Always makes word " + categories.joinToString()
}
