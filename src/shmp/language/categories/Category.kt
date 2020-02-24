package shmp.language.categories

import shmp.language.CategoryRealization
import shmp.language.CategoryValue
import shmp.language.SpeechPart

interface Category {
    val values: List<CategoryValue>
    val possibleValues: Set<CategoryValue>
    val affectedSpeechParts: Set<SpeechPart>
    val outType: String
}

interface CategoryRandomSupplements {
    val mainSpeechPart: SpeechPart

    fun realizationTypeProbability(categoryRealization: CategoryRealization): Double
    fun speechPartProbabilities(speechPart: SpeechPart): Double
}