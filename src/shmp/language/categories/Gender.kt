package shmp.language.categories

import shmp.language.*

val genderOutName = "Gender"

class Gender(
    categories: List<CategoryValue>,
    override val affectedSpeechParts: Set<SpeechPart>
) : AbstractChangeCategory(
    categories,
    GenderValue.values().toSet(),
    genderOutName,
    "Has no genders"
)

fun CategoryRealization.probabilityForGender(): Double = when (this) {//TODO not actual data
    CategoryRealization.PrefixSeparateWord -> 10.0
    CategoryRealization.SuffixSeparateWord -> 10.0
    CategoryRealization.Prefix -> 100.0
    CategoryRealization.Suffix -> 100.0
}

fun SpeechPart.probabilityForGender(): Double = when (this) {
    SpeechPart.Noun -> 0.0
    SpeechPart.Verb -> 100.0
    SpeechPart.Adjective -> 100.0
    SpeechPart.Adverb -> 0.0
    SpeechPart.Numeral -> 0.0
    SpeechPart.Article -> 100.0
    SpeechPart.Pronoun -> 100.0
}

enum class GenderPresence(val probability: Double, val possibilities: List<GenderValue>) {
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

    override val parentClassName = genderOutName
}