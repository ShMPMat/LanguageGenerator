package shmp.lang.language.category

import shmp.lang.language.category.paradigm.CompulsoryData
import shmp.lang.language.category.paradigm.SourcedCategory
import shmp.lang.language.category.realization.CategoryRealization
import shmp.lang.language.category.value.CategoryValue
import shmp.lang.language.category.value.CategoryValues
import shmp.lang.language.category.value.RealizationBox
import shmp.lang.language.lexis.SpeechPart
import shmp.lang.language.syntax.SyntaxRelation
import shmp.random.SampleSpaceObject


interface Category {
    val actualValues: CategoryValues
    val allPossibleValues: Set<CategoryValue>
    val affected: Set<PSpeechPart>
    val speechParts: Set<SpeechPart>
    val staticSpeechParts: Set<SpeechPart>
    val outType: String
}

interface CategoryRandomSupplements {
    fun realizationTypeProbability(categoryRealization: CategoryRealization): Double
    fun speechPartProbabilities(speechPart: SpeechPart): List<SourceTemplate>
    fun specialRealization(values: CategoryValues, speechPart: SpeechPart, categories: List<SourcedCategory>): Set<RealizationBox>
    fun randomRealization(): CategoryValues
    fun randomStaticSpeechParts(): Set<SpeechPart> = emptySet()
    fun randomIsCompulsory(speechPart: SpeechPart): CompulsoryData
    fun getCollapseCoefficient(previousCategoryValues: CategoryValues): Double = 1.0
}

internal fun noValue(probability: Double) = RealizationBox(null, probability)

internal val emptyRealization = setOf(noValue(1.0))

sealed class CategorySource {
    object Self : CategorySource()
    data class Agreement(val relation: SyntaxRelation, val possibleSpeechParts: List<SpeechPart>) : CategorySource()
}

data class PSpeechPart(val speechPart: SpeechPart, val source: CategorySource)

data class SourceTemplate(val source: CategorySource, override val probability: Double): SampleSpaceObject
