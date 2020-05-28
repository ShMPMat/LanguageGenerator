package shmp.language.categories

import shmp.language.CategoryRealization
import shmp.language.CategoryValue
import shmp.language.SpeechPart
import shmp.random.SampleSpaceObject
import kotlin.random.Random

interface Category {
    val actualValues: List<CategoryValue>
    val allPossibleValues: Set<CategoryValue>
    val affectedSpeechParts: Set<SpeechPart>
    val outType: String
}

interface CategoryRandomSupplements {
    fun realizationTypeProbability(categoryRealization: CategoryRealization): Double
    fun speechPartProbabilities(speechPart: SpeechPart): Double
    fun specialRealization(values: List<CategoryValue>, speechPart: SpeechPart): Set<RealizationBox>
    fun randomRealization(random: Random): List<CategoryValue>
}

data class RealizationBox(val realization: CategoryRealization?, override val probability: Double) : SampleSpaceObject {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RealizationBox

        if (realization != other.realization) return false

        return true
    }

    override fun hashCode(): Int {
        return realization?.hashCode() ?: 0
    }
}

internal fun noValue(probability: Double) = RealizationBox(null, probability)

internal val emptyRealization = setOf(noValue(1.0))