package io.tashtabash.lang.language.category

import io.tashtabash.lang.language.category.value.AbstractCategoryValue
import io.tashtabash.lang.language.category.realization.CategoryRealization
import io.tashtabash.lang.language.category.value.CategoryValues
import io.tashtabash.lang.language.category.CategorySource.Agreement
import io.tashtabash.lang.language.category.CategorySource.Self
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


const val animosityName = "Animosity"

class Animosity(
    categories: List<AnimosityValue>,
    affected: Set<PSpeechPart>,
    staticSpeechParts: Set<SpeechPart>
) : AbstractChangeCategory(
    categories,
    AnimosityValue::class.valuesSet(),
    affected,
    staticSpeechParts,
    animosityName
)

object AnimosityRandomSupplements : CategoryRandomSupplements {
    override fun realizationTypeProbability(categoryRealization: CategoryRealization): Double =
        when (categoryRealization) {//TODO not an actual data
            CategoryRealization.PrefixWord -> 20.0
            CategoryRealization.SuffixWord -> 20.0
            CategoryRealization.Prefix -> 100.0
            CategoryRealization.Suffix -> 100.0
            CategoryRealization.Reduplication -> 0.0
            CategoryRealization.Passing -> 0.0
            CategoryRealization.Suppletion -> 0.0
        }

    override fun speechPartProbabilities(speechPart: SpeechPart) = when (speechPart) {
        Noun -> listOf(SourceTemplate(Self, 200.0))//TODO no data at all
        Verb -> listOf(
            SourceTemplate(Agreement(Agent, nominals), 20.0),
            SourceTemplate(Agreement(Patient, nominals), 1.0)
        )
        Adjective -> listOf(SourceTemplate(Agreement(Nominal, nominals), 20.0))
        Adverb -> listOf()
        Numeral -> listOf(SourceTemplate(Agreement(Nominal, nominals), 20.0))
        Article -> listOf(SourceTemplate(Agreement(Agent, nominals), 10.0))
        PersonalPronoun -> listOf(SourceTemplate(Self, 10.0))
        DeixisPronoun -> listOf(SourceTemplate(Self, 10.0))
        Particle -> listOf()
        Adposition -> listOf()
    }

    override fun specialRealization(
        values: CategoryValues,
        speechPart: SpeechPart,
        categories: List<SourcedCategory>
    ): Set<RealizationBox> {
        val acceptableValues = values.filter { it.parentClassName == animosityName }
        if (acceptableValues.size != 1) return emptyRealization
        return when (acceptableValues.first()) {
            AnimosityValue.Inanimate -> setOf(
                RealizationBox(CategoryRealization.Passing, 1.0),
                noValue(1.0)
            )
            else -> return when(speechPart) {
                PersonalPronoun -> setOf(//TODO no actual data
                    noValue(1.0),
                    RealizationBox(CategoryRealization.Suppletion, 2.0)
                )
                DeixisPronoun -> setOf(//TODO no actual data
                    RealizationBox(CategoryRealization.Suffix, 1.5),
                    RealizationBox(CategoryRealization.Prefix, 1.5)
                )
                else -> emptyRealization
            }
        }
    }

    override fun randomRealization() = AnimosityPresence.entries.randomElement().possibilities

    override fun randomStaticSpeechParts() = setOf(Noun)

    override fun randomIsCompulsory(speechPart: SpeechPart) = when (speechPart) {
        Noun -> true
        Verb -> 0.8.testProbability()
        Adjective -> 0.8.testProbability()
        Numeral -> 0.8.testProbability()
        Article -> 0.7.testProbability()
        PersonalPronoun -> 0.4.testProbability()
        DeixisPronoun -> 0.4.testProbability()
        else -> true
    } withCoCategories listOf()
}

enum class AnimosityPresence(
    override val probability: Double,
    val possibilities: List<AnimosityValue>
) : SampleSpaceObject {
    NoAnimosity(100.0, listOf()),
    SimpleAnimosity(10.0, listOf(AnimosityValue.Animate, AnimosityValue.Inanimate))
}

sealed class AnimosityValue(meaning: Meaning, alias: String) : AbstractCategoryValue(animosityName, meaning, alias) {
    object Animate : AnimosityValue("animate indicator", "ANIM")
    object Inanimate : AnimosityValue("inanimate indicator", "INANIM")
}
