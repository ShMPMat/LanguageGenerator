package io.tashtabash.lang.language.category

import io.tashtabash.lang.language.lexis.SpeechPart


data class CategoryPool(val categories: List<Category>) {
    fun getStaticFor(speechPart: SpeechPart) = categories
        .filter { it.actualValues.isNotEmpty() && speechPart in it.staticSpeechParts }
}
