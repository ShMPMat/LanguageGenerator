package io.tashtabash.lang.language.category.value

import io.tashtabash.lang.language.lexis.SemanticsCore


interface CategoryValue {
    val semanticsCore: SemanticsCore
    val parentClassName: String
    val alias: String
}

typealias CategoryValues = List<CategoryValue>
