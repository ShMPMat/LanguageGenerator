package shmp.language.nominal_categories

import shmp.language.*
import shmp.language.nominal_categories.change.CategoryApplicator

class Gender(
    categoryApplicators: Map<NominalCategoryEnum, CategoryApplicator>
) : AbstractChangeNominalCategory(SpeechPart.Noun, categoryApplicators) {
    override fun toString(): String {
        return "Gender:\n" + if (categoryApplicators.isEmpty()) "Has no genders"
        else categoryApplicators.map {
            it.key.toString() + ": " + it.value
        }.joinToString("\n")
    }
}

fun NominalCategoryRealization.probabilityForGender(): Double = when (this) {//TODO not actual data
    NominalCategoryRealization.PrefixSeparateWord -> 10.0
    NominalCategoryRealization.SuffixSeparateWord -> 10.0
    NominalCategoryRealization.Prefix -> 100.0
    NominalCategoryRealization.Suffix -> 100.0
}

enum class GenderPresence(val probability: Double, val possibilities: Set<GenderEnum>) {
    None(145.0, setOf()), //TODO this doesn,t work; there is a Bininj Gun-Wok language with F, M, N and VEGETABLE
    Gendered(84.0, setOf(GenderEnum.Female, GenderEnum.Male, GenderEnum.Neutral, GenderEnum.Common)),
    NonGendered(28.0, GenderEnum.values().filter { !Gendered.possibilities.contains(it) }.toSet())
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