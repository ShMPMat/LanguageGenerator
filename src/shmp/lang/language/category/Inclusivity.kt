package shmp.lang.language.category

import shmp.lang.language.CategoryRealization
import shmp.lang.language.CategoryRealization.*
import shmp.lang.language.CategoryValue
import shmp.lang.language.category.CategorySource.SelfStated
import shmp.lang.language.category.InclusivityValue.Exclusive
import shmp.lang.language.category.InclusivityValue.Inclusive
import shmp.lang.language.category.NumbersValue.*
import shmp.lang.language.category.PersonValue.*
import shmp.lang.language.category.paradigm.SourcedCategory
import shmp.lang.language.category.paradigm.withCoCategories
import shmp.lang.language.lexis.SemanticsCore
import shmp.lang.language.lexis.SpeechPart
import shmp.lang.language.lexis.SpeechPart.*
import shmp.lang.language.lexis.toCluster
import shmp.lang.language.lexis.toUnspecified
import shmp.random.SampleSpaceObject
import shmp.random.singleton.randomElement


const val inclusivityOutName = "Inclusivity"

class Inclusivity(
    categories: List<InclusivityValue>,
    affected: Set<PSpeechPart>,
    staticSpeechParts: Set<SpeechPart>
) : AbstractChangeCategory(
    categories,
    InclusivityValue.values().toSet(),
    affected,
    staticSpeechParts,
    inclusivityOutName
)

object InclusivityRandomSupplements : CategoryRandomSupplements {
    override fun realizationTypeProbability(categoryRealization: CategoryRealization) = when (categoryRealization) {
        //TODO not actual data
        PrefixSeparateWord -> 10.0
        SuffixSeparateWord -> 10.0
        Prefix -> 100.0
        Suffix -> 100.0
        Reduplication -> 0.0
        Passing -> 0.0
        NewWord -> 0.0
    }

    override fun speechPartProbabilities(speechPart: SpeechPart) = when (speechPart) {
        Noun -> listOf()
        Verb -> listOf()
        Adjective -> listOf()
        Adverb -> listOf()
        Numeral -> listOf()
        Article -> listOf()
        PersonalPronoun -> listOf(SourceTemplate(SelfStated, 100.0))
        DeixisPronoun -> listOf()
        Particle -> listOf()
        Adposition -> listOf()
    }

    override fun specialRealization(
        values: List<CategoryValue>,
        speechPart: SpeechPart,
        categories: List<SourcedCategory>
    ) = setOf(//TODO no actual data
        noValue(1.0),
        RealizationBox(NewWord, 3.0)
    )

    override fun randomRealization() = InclusivityPresence.values().randomElement().possibilities

    override fun randomIsCompulsory(speechPart: SpeechPart) = when (speechPart) {
        PersonalPronoun -> true
        else -> false
    } withCoCategories listOf(listOf(Dual, Paucal, Plural), listOf(First))
}

enum class InclusivityPresence(
    override val probability: Double,
    val possibilities: List<InclusivityValue>
) : SampleSpaceObject {
    None(132.0, listOf()),
    Present(68.0, listOf(Inclusive, Exclusive))
}

enum class InclusivityValue(override val semanticsCore: SemanticsCore, override val shortName: String) : CategoryValue {
    Inclusive(SemanticsCore("(inclusive indicator)".toCluster(), Particle.toUnspecified()), "INCL"),
    Exclusive(SemanticsCore("(exclusive indicator)".toCluster(), Particle.toUnspecified()), "EXCL");

    override val parentClassName = inclusivityOutName
}