package shmp.language

data class SyntaxCore(
    val word: String,
    val speechPart: SpeechPart,
    val tags: Set<SyntaxTag>,
    val staticCategories: Set<CategoryValue> = setOf()
)

data class SyntaxTag(val name: String)