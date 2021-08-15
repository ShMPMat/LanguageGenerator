package shmp.lang.language.category

import shmp.lang.language.category.value.AbstractCategoryValue
import shmp.lang.language.category.realization.CategoryRealization
import shmp.lang.language.category.realization.CategoryRealization.*
import shmp.lang.language.category.value.CategoryValues
import shmp.lang.language.category.CategorySource.Agreement
import shmp.lang.language.category.CategorySource.Self
import shmp.lang.language.category.PersonValue.*
import shmp.lang.language.category.paradigm.SourcedCategory
import shmp.lang.language.category.paradigm.withCoCategories
import shmp.lang.language.category.value.RealizationBox
import shmp.lang.language.lexis.Meaning
import shmp.lang.language.lexis.SpeechPart
import shmp.lang.language.lexis.SpeechPart.*
import shmp.lang.language.lexis.SpeechPart.Verb
import shmp.lang.language.lexis.nominals
import shmp.lang.language.syntax.SyntaxRelation.*
import shmp.lang.utils.valuesSet
import shmp.random.SampleSpaceObject
import shmp.random.singleton.randomElement
import shmp.random.singleton.testProbability


const val personName = "Person"

class Person(
    categories: List<PersonValue>,
    affected: Set<PSpeechPart>,
    staticSpeechParts: Set<SpeechPart>
) : AbstractChangeCategory(
    categories,
    PersonValue::class.valuesSet(),
    affected,
    staticSpeechParts,
    personName
)

object PersonRandomSupplements : CategoryRandomSupplements {
    override fun realizationTypeProbability(categoryRealization: CategoryRealization) = when (categoryRealization) {
        //TODO not actual data
        PrefixWord -> 0.0
        SuffixWord -> 0.0
        Prefix -> 1.0
        Suffix -> 1.0
        Reduplication -> 0.0
        Passing -> 0.0
        Suppletion -> 0.0
    }

    override fun speechPartProbabilities(speechPart: SpeechPart) = when (speechPart) {
        Noun -> listOf()
        Verb -> listOf(
            SourceTemplate(Agreement(Agent, nominals), 20.0),
            SourceTemplate(Agreement(Patient, nominals), 1.0)
        )//TODO not an actual data
        Adjective -> listOf(SourceTemplate(Agreement(Nominal, nominals), 20.0))//TODO not an actual data
        Adverb -> listOf()
        Numeral -> listOf()
        Article -> listOf()
        PersonalPronoun -> listOf(SourceTemplate(Self, 200.0))
        DeixisPronoun -> listOf()
        Particle -> listOf()
        Adposition -> listOf(SourceTemplate(Agreement(Agent, nominals), 2.0))
    }

    override fun specialRealization(
        values: CategoryValues,
        speechPart: SpeechPart,
        categories: List<SourcedCategory>
    ): Set<RealizationBox> {
        val acceptableValues = values.filter { it.parentClassName == personName }
        if (acceptableValues.size != 1) return emptyRealization
        val value = acceptableValues.first()
        return when(speechPart) {
            PersonalPronoun -> setOf(//TODO no actual data
                noValue(1.0),
                RealizationBox(Suppletion, 200.0)
            )
            Verb -> setOf(
                noValue(10.0),
                RealizationBox(PrefixWord, 2.0),
                RealizationBox(SuffixWord, 2.0)
            )
            else -> emptyRealization
        }
    }

    override fun randomRealization() = PersonPresence.values().randomElement().possibilities

    override fun randomIsCompulsory(speechPart: SpeechPart) = when (speechPart) {
        Verb -> 0.95.testProbability()
        Adjective -> 0.9.testProbability()
        PersonalPronoun -> true
        Adposition -> 0.7.testProbability()
        else -> true
    } withCoCategories listOf()
}

enum class PersonPresence(override val probability: Double, val possibilities: List<PersonValue>) : SampleSpaceObject {
    ThreePersons(100.0, listOf(First, Second, Third))//TODO too little actual values
}

sealed class PersonValue(meaning: Meaning, alias: String) : AbstractCategoryValue(personName, meaning, alias) {
    object First : PersonValue("(first person ind)", "1")
    object Second : PersonValue("(second person ind)", "2")
    object Third : PersonValue("(third person ind)", "3")
}
