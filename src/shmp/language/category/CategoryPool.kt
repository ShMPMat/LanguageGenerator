package shmp.language.category

import shmp.language.SpeechPart


data class CategoryPool(val categories: List<Category>) {
    fun getStaticFor(speechPart: SpeechPart) = categories
        .filter { it.actualValues.isNotEmpty() && speechPart in it.staticSpeechParts }
}
