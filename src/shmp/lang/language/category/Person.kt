package shmp.lang.language.category

import shmp.lang.language.CategoryRealization
import shmp.lang.language.CategoryRealization.*
import shmp.lang.language.CategoryValue
import shmp.lang.language.lexis.SpeechPart
import shmp.lang.language.lexis.SpeechPart.*
import shmp.lang.language.lexis.SpeechPart.Verb
import shmp.lang.language.category.CategorySource.*
import shmp.lang.language.category.PersonValue.*
import shmp.lang.language.lexis.MeaningCluster
import shmp.lang.language.lexis.SemanticsCore
import shmp.lang.language.lexis.toUnspecified
import shmp.lang.language.syntax.SyntaxRelation.*
import shmp.random.SampleSpaceObject
import shmp.random.singleton.randomElement


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
        PersonalPronoun -> listOf(SourceTemplate(SelfStated, 200.0))
        DeixisPronoun -> listOf()
        Particle -> listOf()
    }

    override fun specialRealization(values: List<CategoryValue>, speechPart: SpeechPart): Set<RealizationBox> {
        val acceptableValues = values.filter { it.parentClassName == outName }
        if (acceptableValues.size != 1) return emptyRealization
        val value = values.first()
        return when(speechPart) {
            PersonalPronoun -> setOf(//TODO no actual data
                noValue(1.0),
                RealizationBox(NewWord, 2.0)
            )
            Verb -> setOf(
                noValue(10.0),
                RealizationBox(PrefixSeparateWord, 2.0),
                RealizationBox(SuffixSeparateWord, 2.0)
            )
            else -> emptyRealization
        }
    }

    override fun randomRealization() = PersonPresence.values().randomElement().possibilities
}

enum class PersonPresence(override val probability: Double, val possibilities: List<PersonValue>) : SampleSpaceObject {
    ThreePersons(100.0, listOf(First, Second, Third))//TODO too little actual values
}

enum class PersonValue(override val semanticsCore: SemanticsCore, override val shortName: String) : CategoryValue {
    First(SemanticsCore(MeaningCluster("(first person indicator)"), Particle.toUnspecified(), setOf()), "1"),
    Second(SemanticsCore(MeaningCluster("(second person indicator)"), Particle.toUnspecified(), setOf()), "2"),
    Third(SemanticsCore(MeaningCluster("(third person indicator)"), Particle.toUnspecified(), setOf()), "3");

    override val parentClassName = outName
}