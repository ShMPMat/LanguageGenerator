package shmp.language.categories

import shmp.language.*
import shmp.language.categories.TenseValue.*
import shmp.random.SampleSpaceObject

private const val outName = "Person"

class Person(
    categories: List<PersonValue>,
    override val affectedSpeechParts: Set<SpeechPart>
) : AbstractChangeCategory(
    categories,
    values().toSet(),
    outName,
    "Has no person"
)

object PersonRandomSupplements : CategoryRandomSupplements {
    override fun realizationTypeProbability(categoryRealization: CategoryRealization) = when (categoryRealization) {
        //TODO not actual data
        CategoryRealization.PrefixSeparateWord -> 0.0
        CategoryRealization.SuffixSeparateWord -> 0.0
        CategoryRealization.Prefix -> 1.0
        CategoryRealization.Suffix -> 1.0
        CategoryRealization.Reduplication -> 0.0
        CategoryRealization.Passing -> 0.0
        CategoryRealization.NewWord -> 100.0
    }

    override fun speechPartProbabilities(speechPart: SpeechPart) = when (speechPart) {
        SpeechPart.Noun -> 0.0
        SpeechPart.Verb -> 10.0//TODO not an actual data
        SpeechPart.Adjective -> 10.0//TODO not an actual data
        SpeechPart.Adverb -> 0.0
        SpeechPart.Numeral -> 0.0
        SpeechPart.Article -> 0.0
        SpeechPart.Pronoun -> 100.0
        SpeechPart.Particle -> 0.0
    }

    override fun specialRealization(values: List<CategoryValue>, speechPart: SpeechPart): Set<RealizationBox> {
        val acceptableValues = values.filter { it.parentClassName == outName }
        if (acceptableValues.size != 1) return emptyRealization
        val value = values.first()
        return when (value) {
            else -> emptyRealization
        }
    }
}

enum class PersonPresence(override val probability: Double, val possibilities: List<PersonValue>) : SampleSpaceObject {
    ThreePersons(100.0, listOf(PersonValue.First, PersonValue.Second, PersonValue.Third))//TODO too little actual values
}

enum class PersonValue(override val syntaxCore: SyntaxCore) : CategoryValue {
    First(SyntaxCore("(first person indicator)", SpeechPart.Particle, setOf())),
    Second(SyntaxCore("(second person indicator)", SpeechPart.Particle, setOf())),
    Third(SyntaxCore("(third person indicator)", SpeechPart.Particle, setOf()));

    override val parentClassName = outName
}