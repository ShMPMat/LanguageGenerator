package shmp.language

data class SyntaxCore(val word: String, val speechPart: SpeechPart, val staticCategories: Set<CategoryEnum> = setOf())