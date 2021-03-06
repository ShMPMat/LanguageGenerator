package shmp.lang.language.category

import shmp.lang.language.CategoryRealization
import shmp.lang.language.CategoryValue
import shmp.lang.language.category.paradigm.CompulsoryData
import shmp.lang.language.category.paradigm.SourcedCategory
import shmp.lang.language.lexis.SpeechPart
import shmp.lang.language.syntax.SyntaxRelation
import shmp.random.SampleSpaceObject
import shmp.random.UnwrappableSSO


interface Category {
    val actualValues: List<CategoryValue>
    val allPossibleValues: Set<CategoryValue>
    val affected: Set<PSpeechPart>
    val speechParts: Set<SpeechPart>
    val staticSpeechParts: Set<SpeechPart>
    val outType: String
}

interface CategoryRandomSupplements {
    fun realizationTypeProbability(categoryRealization: CategoryRealization): Double
    fun speechPartProbabilities(speechPart: SpeechPart): List<SourceTemplate>
    fun specialRealization(values: List<CategoryValue>, speechPart: SpeechPart, categories: List<SourcedCategory>): Set<RealizationBox>
    fun randomRealization(): List<CategoryValue>
    fun randomStaticSpeechParts(): Set<SpeechPart> = emptySet()
    fun randomIsCompulsory(speechPart: SpeechPart): CompulsoryData
    fun getCollapseCoefficient(previousCategoryValues: List<CategoryValue>): Double = 1.0
}

data class RealizationBox(
    val realization: CategoryRealization?,
    override val probability: Double
) : UnwrappableSSO<CategoryRealization?>(realization) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RealizationBox

        if (realization != other.realization) return false

        return true
    }

    override fun hashCode() = realization?.hashCode() ?: 0
}

internal fun noValue(probability: Double) = RealizationBox(null, probability)

internal val emptyRealization = setOf(noValue(1.0))

sealed class CategorySource {
    object SelfStated : CategorySource()
    data class RelationGranted(val relation: SyntaxRelation, val possibleSpeechParts: List<SpeechPart>) : CategorySource()
}

data class PSpeechPart(val speechPart: SpeechPart, val source: CategorySource)

data class SourceTemplate(val source: CategorySource, override val probability: Double): SampleSpaceObject