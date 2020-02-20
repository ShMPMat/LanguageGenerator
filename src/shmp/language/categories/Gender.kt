package shmp.language.categories

import shmp.language.*

class Gender(
    categories: List<NominalCategoryEnum>
) : AbstractChangeCategory(
    categories,
    GenderEnum.values().toSet(),
    "Gender",
    "Has no genders"
)

fun NominalCategoryRealization.probabilityForGender(): Double = when (this) {//TODO not actual data
    NominalCategoryRealization.PrefixSeparateWord -> 10.0
    NominalCategoryRealization.SuffixSeparateWord -> 10.0
    NominalCategoryRealization.Prefix -> 100.0
    NominalCategoryRealization.Suffix -> 100.0
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

enum class GenderPresence(val probability: Double, val possibilities: List<GenderEnum>) {
    None(145.0, listOf()),
    Gendered(84.0, listOf(GenderEnum.Female, GenderEnum.Male, GenderEnum.Neutral, GenderEnum.Common)),
    NonGendered(28.0, GenderEnum.values().toList())
}

enum class GenderEnum(override val syntaxCore: SyntaxCore) : NominalCategoryEnum {
    //TODO adjective, really?
    //TODO more classes
    Female(SyntaxCore("(female gender indicator)", SpeechPart.Adjective)),
    Male(SyntaxCore("(male gender indicator)", SpeechPart.Adjective)),
    Neutral(SyntaxCore("(neutral gender indicator)", SpeechPart.Adjective)),
    Common(SyntaxCore("(common gender indicator)", SpeechPart.Adjective)),

    Person(SyntaxCore("(person class indicator)", SpeechPart.Adjective)),
    Plant(SyntaxCore("(plant class indicator)", SpeechPart.Adjective)),
    Fruit(SyntaxCore("(fruit class indicator)", SpeechPart.Adjective)),
    LongObject(SyntaxCore("(long object class indicator)", SpeechPart.Adjective))
}