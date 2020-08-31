package shmp.language.derivation

import shmp.language.CategoryValue
import shmp.language.lexis.SemanticsCore


interface CategoryChanger {
    fun getNewStaticCategories(semanticsCores: List<SemanticsCore>): Set<CategoryValue>

    val defaultToString: String
}

abstract class AbstractCategoryChanger: CategoryChanger {
    override fun toString() = defaultToString
}


class PassingCategoryChanger(private val index: Int): AbstractCategoryChanger() {
    override fun getNewStaticCategories(semanticsCores: List<SemanticsCore>) =
        semanticsCores[index].staticCategories

    override val defaultToString = "Same categories which a parent have"
}

class ConstantCategoryChanger(val categories: Set<CategoryValue>): AbstractCategoryChanger() {
    override fun getNewStaticCategories(semanticsCores: List<SemanticsCore>) = categories

    override val defaultToString = "Always makes word " + categories.joinToString()
}
