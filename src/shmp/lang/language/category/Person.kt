package shmp.lang.language.category

import shmp.lang.language.CategoryRealization
import shmp.language.*
import shmp.lang.language.CategoryRealization.*
import shmp.lang.language.CategoryValue
import shmp.lang.language.SpeechPart
import shmp.lang.language.SpeechPart.*
import shmp.lang.language.SpeechPart.Verb
import shmp.lang.language.category.CategorySource.*
import shmp.lang.language.category.PersonValue.*
import shmp.lang.language.lexis.MeaningCluster
import shmp.lang.language.lexis.SemanticsCore
import shmp.lang.language.syntax.SyntaxRelation.*
import shmp.random.SampleSpaceObject
import shmp.random.randomElement
import kotlin.random.Random

private const val outName = "Person"

class Person(
    categories: List<PersonValue>,
    affected: Set<PSpeechPart>,
    override val staticSpeechParts: Set<SpeechPart>
) : AbstractChangeCategory(
    categories,
    PersonValue.values().toSet(),
    affected,
    outName
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
        NewWord -> 0.0
    }

    override fun speechPartProbabilities(speechPart: SpeechPart) = when (speechPart) {
        Noun -> listOf()
        Verb -> listOf(
            SourceTemplate(RelationGranted(Subject), 20.0),
            SourceTemplate(RelationGranted(Object), 1.0)
        )//TODO not an actual data
        Adjective -> listOf(SourceTemplate(RelationGranted(Subject), 20.0))//TODO not an actual data
        Adverb -> listOf()
        Numeral -> listOf()
        Article -> listOf()
        Pronoun -> listOf(SourceTemplate(SelfStated, 200.0))
        Particle -> listOf()
    }

    override fun specialRealization(values: List<CategoryValue>, speechPart: SpeechPart): Set<RealizationBox> {
        val acceptableValues = values.filter { it.parentClassName == outName }
        if (acceptableValues.size != 1) return emptyRealization
        val value = values.first()
        return when(speechPart) {
            Pronoun -> setOf(//TODO no actual data
                noValue(1.0),
                RealizationBox(NewWord, 2.0)
            )
            else -> emptyRealization
        }
    }

    override fun randomRealization(random: Random) = randomElement(
        PersonPresence.values(),
        random
    ).possibilities
}

enum class PersonPresence(override val probability: Double, val possibilities: List<PersonValue>) : SampleSpaceObject {
    ThreePersons(100.0, listOf(First, Second, Third))//TODO too little actual values
}

enum class PersonValue(override val semanticsCore: SemanticsCore) : CategoryValue {
    First(SemanticsCore(MeaningCluster("(first person indicator)"), Particle, setOf())),
    Second(SemanticsCore(MeaningCluster("(second person indicator)"), Particle, setOf())),
    Third(SemanticsCore(MeaningCluster("(third person indicator)"), Particle, setOf()));

    override val parentClassName = outName
}