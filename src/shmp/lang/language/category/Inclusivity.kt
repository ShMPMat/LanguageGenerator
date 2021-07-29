package shmp.lang.language.category

import shmp.lang.language.AbstractCategoryValue
import shmp.lang.language.CategoryRealization
import shmp.lang.language.CategoryRealization.*
import shmp.lang.language.CategoryValues
import shmp.lang.language.category.CategorySource.Agreement
import shmp.lang.language.category.CategorySource.Self
import shmp.lang.language.category.InclusivityValue.Exclusive
import shmp.lang.language.category.InclusivityValue.Inclusive
import shmp.lang.language.category.PersonValue.First
import shmp.lang.language.category.paradigm.SourcedCategory
import shmp.lang.language.category.paradigm.withCoCategories
import shmp.lang.language.lexis.Meaning
import shmp.lang.language.lexis.SpeechPart
import shmp.lang.language.lexis.SpeechPart.*
import shmp.lang.language.lexis.nominals
import shmp.lang.language.syntax.SyntaxRelation.Agent
import shmp.lang.utils.values
import shmp.lang.utils.valuesSet
import shmp.random.SampleSpaceObject
import shmp.random.singleton.randomElement
import shmp.random.singleton.testProbability


const val inclusivityName = "Inclusivity"

class Inclusivity(
    categories: List<InclusivityValue>,
    affected: Set<PSpeechPart>,
    staticSpeechParts: Set<SpeechPart>
) : AbstractChangeCategory(
    categories,
    InclusivityValue::class.valuesSet(),
    affected,
    staticSpeechParts,
    inclusivityName
)

object InclusivityRandomSupplements : CategoryRandomSupplements {
    override fun realizationTypeProbability(categoryRealization: CategoryRealization) = when (categoryRealization) {
        //TODO not actual data
        PrefixWord -> 10.0
        SuffixWord -> 10.0
        Prefix -> 100.0
        Suffix -> 100.0
        Reduplication -> 0.0
        Passing -> 0.0
        Suppletion -> 0.0
    }

    override fun speechPartProbabilities(speechPart: SpeechPart) = when (speechPart) {
        Noun -> listOf()
        Verb -> listOf(SourceTemplate(Agreement(Agent, nominals), 100.0))
        Adjective -> listOf()
        Adverb -> listOf()
        Numeral -> listOf()
        Article -> listOf()
        PersonalPronoun -> listOf(SourceTemplate(Self, 50.0))
        DeixisPronoun -> listOf()
        Particle -> listOf()
        Adposition -> listOf()
    }

    override fun specialRealization(
        values: CategoryValues,
        speechPart: SpeechPart,
        categories: List<SourcedCategory>
    ) = if (speechPart == PersonalPronoun) setOf(//TODO no actual data
        noValue(1.0),
        RealizationBox(Suppletion, 3.0)
    ) else emptyRealization

    override fun randomRealization() = InclusivityPresence.values().randomElement().possibilities

    override fun randomIsCompulsory(speechPart: SpeechPart) = when (speechPart) {
        PersonalPronoun -> true withCoCategories listOf(nonSingularNumbers, listOf(First))
        Verb -> 0.99.testProbability() withCoCategories listOf(PersonValue::class.values())
        else -> false withCoCategories listOf()
    }
}

enum class InclusivityPresence(
    override val probability: Double,
    val possibilities: List<InclusivityValue>
) : SampleSpaceObject {
    None(132.0, listOf()),
    Present(68.0, listOf(Inclusive, Exclusive))
}

sealed class InclusivityValue(
    meaning: Meaning,
    alias: String
) : AbstractCategoryValue(inclusivityName, meaning, alias) {
    object Inclusive : InclusivityValue("(inclusive indicator)", "INCL")
    object Exclusive : InclusivityValue("(exclusive indicator)", "EXCL")
}
