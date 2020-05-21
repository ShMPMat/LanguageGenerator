package shmp.language.categories

import shmp.language.*
import shmp.random.SampleSpaceObject

const val genderName = "Gender"

class Gender(
    categories: List<GenderValue>,
    override val affectedSpeechParts: Set<SpeechPart>
) : AbstractChangeCategory(
    categories,
    GenderValue.values().toSet(),
    genderName,
    "Has no genders"
)

object GenderRandomSupplements : CategoryRandomSupplements {
    override val mainSpeechPart: SpeechPart = SpeechPart.Noun

    override fun realizationTypeProbability(categoryRealization: CategoryRealization): Double =
        when (categoryRealization) {//TODO not actual data
            CategoryRealization.PrefixSeparateWord -> 10.0
            CategoryRealization.SuffixSeparateWord -> 10.0
            CategoryRealization.Prefix -> 100.0
            CategoryRealization.Suffix -> 100.0
            CategoryRealization.Reduplication -> 0.0
            CategoryRealization.Passing -> 0.0
        }

    override fun speechPartProbabilities(speechPart: SpeechPart): Double =
        when (speechPart) {
            SpeechPart.Noun -> 0.0
            SpeechPart.Verb -> 100.0
            SpeechPart.Adjective -> 100.0
            SpeechPart.Adverb -> 0.0
            SpeechPart.Numeral -> 0.0
            SpeechPart.Article -> 100.0
            SpeechPart.Pronoun -> 100.0
            SpeechPart.Particle -> 0.0
        }

    override fun specialRealization(values: List<CategoryValue>, speechPart: SpeechPart): Set<RealizationBox> {
        val acceptableValues = values.filter { it.parentClassName == genderName }
        if (acceptableValues.size != 1) return emptyRealization
        val value = values.first()
        return when(value) {
            else -> emptyRealization
        }
    }
}

enum class GenderPresence(override val probability: Double, val possibilities: List<GenderValue>): SampleSpaceObject {
    None(145.0, listOf()),
    FmcnGendered(6.0, listOf(GenderValue.Common, GenderValue.Female, GenderValue.Male, GenderValue.Neutral)),
    FmnGendered(26.0, listOf(GenderValue.Female, GenderValue.Male, GenderValue.Neutral)),
    CnGendered(26.0, listOf(GenderValue.Common, GenderValue.Neutral)),
    FmGendered(26.0, listOf(GenderValue.Female, GenderValue.Male)),
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