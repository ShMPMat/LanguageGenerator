package shmp.language.lexis

import shmp.language.CategoryValue
import shmp.language.SpeechPart

data class SemanticsCore(
    val word: String,
    val speechPart: SpeechPart,
    val tags: Set<SemanticsTag>,
    val staticCategories: Set<CategoryValue> = setOf()
)

data class SemanticsTag(val name: String)