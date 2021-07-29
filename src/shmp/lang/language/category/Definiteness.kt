package shmp.lang.language.category

import shmp.lang.language.AbstractCategoryValue
import shmp.lang.language.CategoryRealization
import shmp.lang.language.CategoryValues
import shmp.lang.language.category.CategorySource.Agreement
import shmp.lang.language.category.CategorySource.Self
import shmp.lang.language.category.DefinitenessValue.Definite
import shmp.lang.language.category.DefinitenessValue.Indefinite
import shmp.lang.language.category.paradigm.SourcedCategory
import shmp.lang.language.category.paradigm.withCoCategories
import shmp.lang.language.lexis.Meaning
import shmp.lang.language.lexis.SpeechPart
import shmp.lang.language.lexis.SpeechPart.*
import shmp.lang.language.lexis.nominals
import shmp.lang.language.syntax.SyntaxRelation.Nominal
import shmp.lang.utils.valuesSet
import shmp.random.SampleSpaceObject
import shmp.random.singleton.randomElement
import shmp.random.singleton.testProbability


const val definitenessName = "Definiteness"

class Definiteness(
    categories: List<DefinitenessValue>,
    affected: Set<PSpeechPart>,
    staticSpeechParts: Set<SpeechPart>
) : AbstractChangeCategory(
    categories,
    DefinitenessValue::class.valuesSet(),
    affected,
    staticSpeechParts,
    definitenessName
)

object DefinitenessRandomSupplements : CategoryRandomSupplements {
    override fun realizationTypeProbability(categoryRealization: CategoryRealization): Double =
        when (categoryRealization) {//TODO not an actual data
            CategoryRealization.PrefixSeparateWord -> 400.0
            CategoryRealization.SuffixSeparateWord -> 20.0
            CategoryRealization.Prefix -> 100.0
            CategoryRealization.Suffix -> 30.0
            CategoryRealization.Reduplication -> 0.0
            CategoryRealization.Passing -> 0.0
            CategoryRealization.NewWord -> 0.0
        }

    override fun speechPartProbabilities(speechPart: SpeechPart) = when (speechPart) {
        Noun -> listOf(SourceTemplate(Self, 500.0))
        Verb -> listOf()
        Adjective -> listOf(SourceTemplate(Agreement(Nominal, nominals), 100.0))
        Adverb -> listOf()
        Numeral -> listOf()
        Article -> listOf()
        PersonalPronoun -> listOf()
        DeixisPronoun -> listOf()
        Particle -> listOf()
        Adposition -> listOf()
    }

    override fun specialRealization(
        values: CategoryValues,
        speechPart: SpeechPart,
        categories: List<SourcedCategory>
    ): Set<RealizationBox> {
        if (speechPart == Adjective)
            return setOf(
                RealizationBox(CategoryRealization.Prefix, 1.0),
                RealizationBox(CategoryRealization.Suffix, 1.0)
            )

        val acceptableValues = values.filter { it.parentClassName == definitenessName }
        if (acceptableValues.size != 1) return emptyRealization

        return when(acceptableValues.first()) {
            else -> emptyRealization
        }
    }

    override fun randomRealization() = DefinitenessPresence.values().randomElement().possibilities

    override fun randomIsCompulsory(speechPart: SpeechPart) = when (speechPart) {
        Noun -> 0.8.testProbability()
        Adjective -> 0.7.testProbability()
        else -> true
    } withCoCategories listOf()
}

enum class DefinitenessPresence(
    override val probability: Double,
    val possibilities: List<DefinitenessValue>
) : SampleSpaceObject {
    NoDefiniteness(198.0, listOf()),
    OnlyDefinite(98.0, listOf(Definite)),
    OnlyIndefinite(45.0, listOf(Indefinite)),
    DefiniteAndIndefinite(209.0, listOf(Definite, Indefinite))
}

sealed class DefinitenessValue(meaning: Meaning, alias: String) : AbstractCategoryValue(
    definitenessName,
    meaning,
    alias,
    Article
) {
    object Definite : DefinitenessValue("the", "DEF")
    object Indefinite : DefinitenessValue("a", "INDEF")
}
