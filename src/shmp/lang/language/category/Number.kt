package shmp.lang.language.category

import shmp.lang.language.AbstractCategoryValue
import shmp.lang.language.CategoryRealization
import shmp.lang.language.CategoryRealization.*
import shmp.lang.language.CategoryValues
import shmp.lang.language.category.CategorySource.Agreement
import shmp.lang.language.category.CategorySource.Self
import shmp.lang.language.category.NumberValue.Plural
import shmp.lang.language.category.NumberValue.Singular
import shmp.lang.language.category.paradigm.SourcedCategory
import shmp.lang.language.category.paradigm.withCoCategories
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


const val numberName = "Number"

class Number(
    categories: List<NumberValue>,
    affected: Set<PSpeechPart>,
    staticSpeechParts: Set<SpeechPart>
) : AbstractChangeCategory(
    categories,
    NumberValue::class.valuesSet(),
    affected,
    staticSpeechParts,
    numberName
)

object NumberRandomSupplements : CategoryRandomSupplements {
    override fun realizationTypeProbability(categoryRealization: CategoryRealization) = when (categoryRealization) {
        //TODO not actual data
        PrefixSeparateWord -> 10.0
        SuffixSeparateWord -> 10.0
        Prefix -> 100.0
        Suffix -> 100.0
        Reduplication -> 0.0
        Passing -> 0.0
        NewWord -> 0.0
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
        return when (values.first()) {
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
            else -> when (speechPart) {
                PersonalPronoun -> setOf(//TODO no actual data
                    noValue(1.0),
                    RealizationBox(NewWord, 1.3)
                )
                DeixisPronoun -> setOf(//TODO no actual data
                    RealizationBox(Suffix, 1.5),
                    RealizationBox(Prefix, 1.5)
                )
                else -> emptyRealization
            }
        }
    }

    override fun randomRealization() = NumberPresence.values().randomElement().possibilities

    override fun randomIsCompulsory(speechPart: SpeechPart) = when (speechPart) {
        Noun -> 0.95.testProbability()
        Verb -> 0.95.testProbability()
        Adjective -> 0.9.testProbability()
        Numeral -> 0.9.testProbability()
        Article -> 0.9.testProbability()
        PersonalPronoun -> 0.9.testProbability()
        DeixisPronoun -> 0.9.testProbability()
        Adposition -> 0.7.testProbability()
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
    object Dual : NumberValue("(dual number indicator)", "DL")
    object Paucal : NumberValue("(paucal number indicator)", "PC")
    object Plural : NumberValue("(plural number indicator)", "PL")
}

val nonSingularNumbers = listOf(NumberValue.Dual, NumberValue.Paucal, Plural)
