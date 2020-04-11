package shmp.language.categories

import shmp.language.CategoryRealization
import shmp.language.CategoryValue
import shmp.language.SpeechPart
import shmp.random.SampleSpaceObject

interface Category {
    val values: List<CategoryValue>
    val possibleValues: Set<CategoryValue>
    val affectedSpeechParts: Set<SpeechPart>
    val outType: String
}

interface CategoryRandomSupplements<E: CategoryValue> {
    val mainSpeechPart: SpeechPart

    fun realizationTypeProbability(categoryRealization: CategoryRealization): Double
    fun speechPartProbabilities(speechPart: SpeechPart): Double
    fun specialRealization(value: CategoryValue): Set<CategoryValueBox<E>>
}

data class CategoryValueBox<E: CategoryValue>(val value: E?, override val probability: Double): SampleSpaceObject