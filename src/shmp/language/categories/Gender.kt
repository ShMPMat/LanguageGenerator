package shmp.language.categories

import shmp.language.*
import shmp.random.SampleSpaceObject

private const val outName = "Gender"

class Gender(
    categories: List<GenderValue>,
    override val affectedSpeechParts: Set<SpeechPart>
) : AbstractChangeCategory(
    categories,
    GenderValue.values().toSet(),
    outName,
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
        }
}

enum class GenderPresence(override val probability: Double, val possibilities: List<GenderValue>): SampleSpaceObject {
    //TODO choose subset, not entire list
    None(145.0, listOf()),
    Gendered(84.0, listOf(GenderValue.Female, GenderValue.Male, GenderValue.Neutral, GenderValue.Common)),
    NonGendered(28.0, GenderValue.values().toList())
}

enum class GenderValue(override val syntaxCore: SyntaxCore) : CategoryValue {
    //TODO adjective, really?
    //TODO more classes
    Female(SyntaxCore("(female gender indicator)", SpeechPart.Adjective)),
    Male(SyntaxCore("(male gender indicator)", SpeechPart.Adjective)),
    Neutral(SyntaxCore("(neutral gender indicator)", SpeechPart.Adjective)),
    Common(SyntaxCore("(common gender indicator)", SpeechPart.Adjective)),

    Person(SyntaxCore("(person class indicator)", SpeechPart.Adjective)),
    Plant(SyntaxCore("(plant class indicator)", SpeechPart.Adjective)),
    Fruit(SyntaxCore("(fruit class indicator)", SpeechPart.Adjective)),
    LongObject(SyntaxCore("(long object class indicator)", SpeechPart.Adjective));

    override val parentClassName = outName
}