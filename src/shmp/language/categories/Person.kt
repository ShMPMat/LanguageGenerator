package shmp.language.categories

import shmp.language.*
import shmp.language.CategoryRealization.*
import shmp.language.SpeechPart.*
import shmp.language.categories.PersonValue.*
import shmp.random.SampleSpaceObject

private const val outName = "Person"

class Person(
    categories: List<PersonValue>,
    override val affectedSpeechParts: Set<SpeechPart>
) : AbstractChangeCategory(
    categories,
    PersonValue.values().toSet(),
    outName,
    "Has no person"
)

object PersonRandomSupplements : CategoryRandomSupplements {
    override fun realizationTypeProbability(categoryRealization: CategoryRealization) = when (categoryRealization) {
        //TODO not actual data
        PrefixSeparateWord -> 0.0
        SuffixSeparateWord -> 0.0
        Prefix -> 1.0
        Suffix -> 1.0
        Reduplication -> 0.0
        Passing -> 0.0
        NewWord -> 100.0
    }

    override fun speechPartProbabilities(speechPart: SpeechPart) = when (speechPart) {
        Noun -> 0.0
        Verb -> 10.0//TODO not an actual data
        Adjective -> 10.0//TODO not an actual data
        Adverb -> 0.0
        Numeral -> 0.0
        Article -> 0.0
        Pronoun -> 100.0
        Particle -> 0.0
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
    ThreePersons(100.0, listOf(First, Second, Third))//TODO too little actual values
}

enum class PersonValue(override val syntaxCore: SyntaxCore) : CategoryValue {
    First(SyntaxCore("(first person indicator)", Particle, setOf())),
    Second(SyntaxCore("(second person indicator)", Particle, setOf())),
    Third(SyntaxCore("(third person indicator)", Particle, setOf()));

    override val parentClassName = outName
}