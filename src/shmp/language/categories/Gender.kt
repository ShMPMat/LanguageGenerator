package shmp.language.categories

import shmp.language.*
import shmp.language.categories.GenderValue.*
import shmp.random.SampleSpaceObject
import shmp.random.randomElement
import shmp.random.randomSublist
import kotlin.random.Random

const val genderName = "Gender"

class Gender(
    categories: List<GenderValue>,
    override val affectedSpeechParts: Set<SpeechPart>
) : AbstractChangeCategory(
    categories,
    values().toSet(),
    genderName,
    "Has no genders"
)

object GenderRandomSupplements : CategoryRandomSupplements {
    override fun realizationTypeProbability(categoryRealization: CategoryRealization): Double =
        when (categoryRealization) {//TODO not actual data
            CategoryRealization.PrefixSeparateWord -> 10.0
            CategoryRealization.SuffixSeparateWord -> 10.0
            CategoryRealization.Prefix -> 100.0
            CategoryRealization.Suffix -> 100.0
            CategoryRealization.Reduplication -> 0.0
            CategoryRealization.Passing -> 0.0
            CategoryRealization.NewWord -> 0.0
        }

    override fun speechPartProbabilities(speechPart: SpeechPart): Double =
        when (speechPart) {
            SpeechPart.Noun -> 100.0
            SpeechPart.Verb -> 95.0
            SpeechPart.Adjective -> 95.0
            SpeechPart.Adverb -> 0.0
            SpeechPart.Numeral -> 0.0
            SpeechPart.Article -> 90.0
            SpeechPart.Pronoun -> 99.0
            SpeechPart.Particle -> 0.0
        }

    override fun specialRealization(values: List<CategoryValue>, speechPart: SpeechPart): Set<RealizationBox> {
        val acceptableValues = values.filter { it.parentClassName == genderName }
        if (acceptableValues.size != 1) return emptyRealization
        val value = values.first()
        return when(speechPart) {
            SpeechPart.Pronoun -> setOf(//TODO no actual data
                noValue(1.0),
                RealizationBox(CategoryRealization.NewWord, 2.0)
            )
            else -> emptyRealization
        }
    }

    override fun randomRealization(random: Random): List<GenderValue> {
        val type = randomElement(
            GenderPresence.values(),
            random
        )
        return if (type == GenderPresence.NonGendered)
            randomSublist(type.possibilities, random, min = 2)
        else
            type.possibilities
    }
}

enum class GenderPresence(override val probability: Double, val possibilities: List<GenderValue>): SampleSpaceObject {
    None(145.0, listOf()),
    FmcnGendered(6.0, listOf(Common, Female, Male, Neutral)),
    FmnGendered(26.0, listOf(Female, Male, Neutral)),
    CnGendered(26.0, listOf(Common, Neutral)),
    FmGendered(26.0, listOf(Female, Male)),
    NonGendered(28.0, GenderValue.values().toList())
}

enum class GenderValue(override val syntaxCore: SyntaxCore) : CategoryValue {
    //TODO more classes (don't forget to add tags for words after it!)
    Female(SyntaxCore("(female gender indicator)", SpeechPart.Particle, setOf())),
    Male(SyntaxCore("(male gender indicator)", SpeechPart.Particle, setOf())),
    Neutral(SyntaxCore("(neutral gender indicator)", SpeechPart.Particle, setOf())),
    Common(SyntaxCore("(common gender indicator)", SpeechPart.Particle, setOf())),

    Person(SyntaxCore("(person class indicator)", SpeechPart.Particle, setOf())),
    Plant(SyntaxCore("(plant class indicator)", SpeechPart.Particle, setOf())),
    Fruit(SyntaxCore("(fruit class indicator)", SpeechPart.Particle, setOf())),
    LongObject(SyntaxCore("(long object class indicator)", SpeechPart.Particle, setOf()));

    override val parentClassName = genderName
}