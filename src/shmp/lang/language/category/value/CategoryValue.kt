package shmp.lang.language.category.value

import shmp.lang.language.lexis.SemanticsCore


interface CategoryValue {
    val semanticsCore: SemanticsCore
    val parentClassName: String
    val alias: String
}

typealias CategoryValues = List<CategoryValue>
