package shmp.lang.language.category

import shmp.lang.language.lexis.SpeechPart


data class CategoryPool(val categories: List<Category>) {
    fun getStaticFor(speechPart: SpeechPart) = categories
        .filter { it.actualValues.isNotEmpty() && speechPart in it.staticSpeechParts }
}
