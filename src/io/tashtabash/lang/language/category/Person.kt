package io.tashtabash.lang.language.category

import io.tashtabash.lang.language.category.value.AbstractCategoryValue
import io.tashtabash.lang.language.category.realization.CategoryRealization
import io.tashtabash.lang.language.category.realization.CategoryRealization.*
import io.tashtabash.lang.language.category.value.CategoryValues
import io.tashtabash.lang.language.category.CategorySource.Agreement
import io.tashtabash.lang.language.category.CategorySource.Self
import io.tashtabash.lang.language.category.PersonValue.*
import io.tashtabash.lang.language.category.paradigm.SourcedCategory
import io.tashtabash.lang.language.category.paradigm.withCoCategories
import io.tashtabash.lang.language.category.value.RealizationBox
import io.tashtabash.lang.language.lexis.Meaning
import io.tashtabash.lang.language.lexis.SpeechPart
import io.tashtabash.lang.language.lexis.SpeechPart.*
import io.tashtabash.lang.language.lexis.SpeechPart.Verb
import io.tashtabash.lang.language.lexis.nominals
import io.tashtabash.lang.language.syntax.SyntaxRelation.*
import io.tashtabash.lang.utils.valuesSet
import io.tashtabash.random.SampleSpaceObject
import io.tashtabash.random.singleton.randomElement
import io.tashtabash.random.singleton.testProbability


const val personName = "Person"

class Person(
    categories: List<PersonValue>,
    affected: Set<PSpeechPart>,
    staticSpeechParts: Set<SpeechPart>
) : AbstractChangeCategory<PersonValue>(
    categories,
    PersonValue::class.valuesSet(),
    affected,
    staticSpeechParts,
    personName
)

object PersonRandomSupplements : CategoryRandomSupplements<PersonValue> {
    override fun realizationTypeProbability(categoryRealization: CategoryRealization) = when (categoryRealization) {
        // I wasn't able to find the data on the distribution
        PrefixWord -> 0.0
        SuffixWord -> 0.0
        Prefix -> 1.0
        Suffix -> 1.0
        Reduplication -> .0
        Passing -> .0
        Suppletion -> .0
    }

    override fun speechPartProbabilities(speechPart: SpeechPart) = when (speechPart) {
        Noun -> listOf()
        Verb -> listOf(
            SourceTemplate(Agreement(Agent, nominals), 20.0),
            SourceTemplate(Agreement(Patient, nominals), 1.0)
        )// I wasn't able to find the data on the distribution
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
        if (acceptableValues.size != 1)
            return emptyRealization

        return when(speechPart) {
            PersonalPronoun -> setOf(
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

    override fun randomRealization() = PersonPresence.entries.randomElement().possibilities

    override fun randomIsCompulsory(speechPart: SpeechPart) = when (speechPart) {
        Verb -> .95.testProbability()
        Adjective -> .9.testProbability()
        PersonalPronoun -> true
        Adposition -> .7.testProbability()
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
