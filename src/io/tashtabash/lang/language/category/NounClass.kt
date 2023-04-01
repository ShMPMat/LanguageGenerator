package io.tashtabash.lang.language.category

import io.tashtabash.lang.language.category.value.AbstractCategoryValue
import io.tashtabash.lang.language.category.realization.CategoryRealization
import io.tashtabash.lang.language.category.value.CategoryValues
import io.tashtabash.lang.language.category.CategorySource.Agreement
import io.tashtabash.lang.language.category.CategorySource.Self
import io.tashtabash.lang.language.category.NounClassValue.*
import io.tashtabash.lang.language.category.paradigm.SourcedCategory
import io.tashtabash.lang.language.category.paradigm.withCoCategories
import io.tashtabash.lang.language.category.value.RealizationBox
import io.tashtabash.lang.language.lexis.Meaning
import io.tashtabash.lang.language.lexis.SpeechPart
import io.tashtabash.lang.language.lexis.SpeechPart.*
import io.tashtabash.lang.language.lexis.nominals
import io.tashtabash.lang.language.syntax.SyntaxRelation
import io.tashtabash.lang.utils.values
import io.tashtabash.lang.utils.valuesSet
import io.tashtabash.random.SampleSpaceObject
import io.tashtabash.random.randomSublist
import io.tashtabash.random.singleton.RandomSingleton
import io.tashtabash.random.singleton.randomElement
import io.tashtabash.random.singleton.testProbability


const val nounClassName = "NounClass"

class NounClass(
    categories: List<NounClassValue>,
    affected: Set<PSpeechPart>,
    staticSpeechParts: Set<SpeechPart>
) : AbstractChangeCategory(
    categories,
    NounClassValue::class.valuesSet(),
    affected,
    staticSpeechParts,
    nounClassName
)

class NounClassRandomSupplements : CategoryRandomSupplements {
    override fun realizationTypeProbability(categoryRealization: CategoryRealization): Double =
        when (categoryRealization) {//TODO not actual data
            CategoryRealization.PrefixWord -> 10.0
            CategoryRealization.SuffixWord -> 10.0
            CategoryRealization.Prefix -> 100.0
            CategoryRealization.Suffix -> 100.0
            CategoryRealization.Reduplication -> 0.0
            CategoryRealization.Passing -> 0.0
            CategoryRealization.Suppletion -> 0.0
        }

    private val nounProbability = RandomSingleton.random.nextDouble(90.0, 100.0)
    private val pronounProbability = RandomSingleton.random.nextDouble(90.0, 100.0)

    override fun speechPartProbabilities(speechPart: SpeechPart) = when (speechPart) {
        Noun -> listOf(SourceTemplate(Self, nounProbability))
        Verb -> listOf(
            SourceTemplate(Agreement(SyntaxRelation.Agent, nominals), 95.0),
            SourceTemplate(Agreement(SyntaxRelation.Patient, nominals), 5.0)
        )
        Adjective -> listOf(SourceTemplate(Agreement(SyntaxRelation.Nominal, nominals), 95.0))
        Adverb -> listOf()
        Numeral -> listOf(SourceTemplate(Agreement(SyntaxRelation.Nominal, nominals), 95.0))
        Article -> listOf(SourceTemplate(Agreement(SyntaxRelation.Agent, nominals), 90.0))
        PersonalPronoun -> listOf(SourceTemplate(Self, pronounProbability))
        DeixisPronoun -> listOf(SourceTemplate(Self, 90.0))
        Particle -> listOf()
        Adposition -> listOf(SourceTemplate(Agreement(SyntaxRelation.Agent, nominals), 2.0))
    }

    override fun specialRealization(
        values: CategoryValues,
        speechPart: SpeechPart,
        categories: List<SourcedCategory>
    ): Set<RealizationBox> {
        val acceptableValues = values.filter { it.parentClassName == nounClassName }
        if (acceptableValues.size != 1) return emptyRealization
        val value = acceptableValues.first()
        return when (speechPart) {
            PersonalPronoun -> setOf(//TODO no actual data
                noValue(1.0),
                RealizationBox(CategoryRealization.Suppletion, 3.0)
            )
            DeixisPronoun -> setOf(//TODO no actual data
                RealizationBox(CategoryRealization.Suffix, 1.5),
                RealizationBox(CategoryRealization.Prefix, 1.5)
            )
            else -> emptyRealization
        }
    }

    override fun randomRealization(): List<NounClassValue> {
        val type = NounClassPresence.values().randomElement()

        return if (type == NounClassPresence.NonGendered)
            randomSublist(type.possibilities, RandomSingleton.random, min = 4)
        else
            type.possibilities
    }

    override fun randomStaticSpeechParts() = setOf(Noun)

    override fun randomIsCompulsory(speechPart: SpeechPart) = when (speechPart) {
        Noun -> true
        Verb -> 0.95.testProbability()
        Adjective -> 0.9.testProbability()
        Numeral -> 0.9.testProbability()
        Article -> 0.9.testProbability()
        PersonalPronoun -> 0.8.testProbability()
        DeixisPronoun -> 0.8.testProbability()
        Adposition -> 0.6.testProbability()
        else -> true
    } withCoCategories listOf()

    override fun getCollapseCoefficient(previousCategoryValues: CategoryValues): Double {
        var result = super.getCollapseCoefficient(previousCategoryValues)

        if (previousCategoryValues.isEmpty())
            return result

        if (previousCategoryValues.any { it in nonSingularNumbers })
            result /= 20
        if (previousCategoryValues.any { it in listOf(PersonValue.First, PersonValue.Second) })
            result /= 100

        return result
    }
}

enum class NounClassPresence(override val probability: Double, val possibilities: List<NounClassValue>) :
    SampleSpaceObject {
    None(145.0, listOf()),
    FmcnGendered(6.0, listOf(Common, Female, Male, Neutral)),
    FmnGendered(26.0, listOf(Female, Male, Neutral)),
    CnGendered(26.0, listOf(Common, Neutral)),
    FmGendered(26.0, listOf(Female, Male)),
    NonGendered(28.0, NounClassValue::class.values())
}

sealed class NounClassValue(meaning: Meaning, alias: String) : AbstractCategoryValue(nounClassName, meaning, alias) {
    //TODO more classes (don't forget to add tags for words after it!)
    object Female : NounClassValue("(female class ind)", "FEM")
    object Male : NounClassValue("(male class ind)", "MALE")
    object Neutral : NounClassValue("(neutral class ind)", "NEUT")
    object Common : NounClassValue("(common class ind)", "COMM")
    object Person : NounClassValue("(person class ind)", "PERS")
    object Plant : NounClassValue("(plant class ind)", "PLANT")
    object Fruit : NounClassValue("(fruit class ind)", "FRUIT")
    object LongObject : NounClassValue("(long object class ind)", "LONG.OBJ")
}
