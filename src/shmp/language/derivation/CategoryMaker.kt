package shmp.language.derivation

import shmp.language.CategoryValue
import shmp.language.lexis.SemanticsCore

interface CategoryMaker {
    fun getNewStaticCategories(semanticsCore: SemanticsCore): Set<CategoryValue>

    val defaultToString: String
}

abstract class AbstractCategoryMaker: CategoryMaker {
    override fun toString() = defaultToString
}


object PassingCategoryMaker: AbstractCategoryMaker() {
    override fun getNewStaticCategories(semanticsCore: SemanticsCore) = semanticsCore.staticCategories

    override val defaultToString = "The same categories which a parent have"
}

class ConstantCategoryMaker(val categories: Set<CategoryValue>): AbstractCategoryMaker() {
    override fun getNewStaticCategories(semanticsCore: SemanticsCore) = categories

    override val defaultToString = "Always makes word " + categories.joinToString()
}
