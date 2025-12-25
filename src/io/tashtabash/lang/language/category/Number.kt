package io.tashtabash.lang.language.category

import io.tashtabash.lang.language.category.value.AbstractCategoryValue
import io.tashtabash.lang.language.category.realization.CategoryRealization
import io.tashtabash.lang.language.category.realization.CategoryRealization.*
import io.tashtabash.lang.language.category.value.CategoryValues
import io.tashtabash.lang.language.category.CategorySource.Agreement
import io.tashtabash.lang.language.category.CategorySource.Self
import io.tashtabash.lang.language.category.NumberValue.Plural
import io.tashtabash.lang.language.category.NumberValue.Singular
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


const val numberName = "Number"

class Number(
    categories: List<NumberValue>,
    affected: Set<PSpeechPart>,
    staticSpeechParts: Set<SpeechPart> = setOf()
) : AbstractChangeCategory<NumberValue>(
    categories,
    NumberValue::class.valuesSet(),
    affected,
    staticSpeechParts,
    numberName
)

object NumberRandomSupplements : CategoryRandomSupplements<NumberValue> {
    override fun realizationTypeProbability(categoryRealization: CategoryRealization) = when (categoryRealization) {
        // I wasn't able to find the data on the distribution
        PrefixWord -> 10.0
        SuffixWord -> 10.0
        Prefix -> 100.0
        Suffix -> 100.0
        Reduplication -> .0
        Passing -> .0
        Suppletion -> .0
    }

    override fun speechPartProbabilities(speechPart: SpeechPart) = when (speechPart) {
        Noun -> listOf(SourceTemplate(Self, 100.0))
        Verb -> listOf(
            SourceTemplate(Agreement(Agent, nominals), 99.0),
            SourceTemplate(Agreement(Patient, nominals), 5.0)
        )
        Adjective -> listOf(SourceTemplate(Agreement(Nominal, nominals), 99.0))
        Adverb -> listOf()
        Numeral -> listOf(SourceTemplate(Agreement(Nominal, nominals), 90.0))
        Article -> listOf(SourceTemplate(Agreement(Agent, nominals), 10.0))
        PersonalPronoun -> listOf(SourceTemplate(Self, 99.0))
        DeixisPronoun -> listOf(SourceTemplate(Self, 99.0))
        Particle -> listOf()
        Adposition -> listOf(SourceTemplate(Agreement(Agent, nominals), 2.0))
    }

    override fun specialRealization(
        values: CategoryValues,
        speechPart: SpeechPart,
        categories: List<SourcedCategory>
    ): Set<RealizationBox> {
        val acceptableValues = values.filter { it.parentClassName == numberName }
        if (acceptableValues.size != 1) return emptyRealization
        return when (acceptableValues.first()) {
            Singular -> setOf(
                noValue(1.0),
                RealizationBox(Passing, 1.0)
            )
            Plural -> when (speechPart) {
                in setOf(Noun, PersonalPronoun) -> setOf(
                    noValue(1.0),
                    RealizationBox(Reduplication, 1.0)
                )
                else -> emptyRealization
            }
            else -> when (speechPart) {// I wasn't able to find the data on the distribution
                PersonalPronoun -> setOf(
                    noValue(1.0),
                    RealizationBox(Suppletion, 1.3)
                )
                DeixisPronoun -> setOf(
                    RealizationBox(Suffix, 1.5),
                    RealizationBox(Prefix, 1.5)
                )
                else -> emptyRealization
            }
        }
    }

    override fun randomRealization() = NumberPresence.entries.randomElement().possibilities

    override fun randomIsCompulsory(speechPart: SpeechPart) = when (speechPart) {
        Noun -> .95.testProbability()
        Verb -> .95.testProbability()
        Adjective -> .9.testProbability()
        Numeral -> .9.testProbability()
        Article -> .9.testProbability()
        PersonalPronoun -> .9.testProbability()
        DeixisPronoun -> .9.testProbability()
        Adposition -> .7.testProbability()
        else -> true
    } withCoCategories listOf()
}

enum class NumberPresence(
    override val probability: Double,
    val possibilities: List<NumberValue>
) : SampleSpaceObject {
    None(100.0, listOf()),
    Plural(180.0, listOf(Singular, NumberValue.Plural)),
    Dual(20.0, listOf(Singular, NumberValue.Dual, NumberValue.Plural)),
    Paucal(10.0, listOf(Singular, NumberValue.Paucal, NumberValue.Plural)),
    PaucalDual(2.0, listOf(Singular, NumberValue.Dual, NumberValue.Paucal, NumberValue.Plural))
}

sealed class NumberValue(meaning: Meaning, alias: String) : AbstractCategoryValue(numberName, meaning, alias) {
    object Singular : NumberValue("(singular number indicator)", "SN")
    object Dual : NumberValue("(dual number indicator)", "DU")
    object Paucal : NumberValue("(paucal number indicator)", "PC")
    object Plural : NumberValue("(plural number indicator)", "PL")
}

val nonSingularNumbers = listOf(NumberValue.Dual, NumberValue.Paucal, Plural)
