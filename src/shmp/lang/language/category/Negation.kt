package shmp.lang.language.category

import shmp.lang.language.CategoryRealization
import shmp.lang.language.CategoryValue
import shmp.lang.language.category.CategorySource.SelfStated
import shmp.lang.language.lexis.SemanticsCore
import shmp.lang.language.lexis.SpeechPart
import shmp.lang.language.lexis.SpeechPart.*
import shmp.lang.language.lexis.toCluster
import shmp.lang.language.lexis.toUnspecified
import shmp.random.SampleSpaceObject
import shmp.random.singleton.randomElement

private const val outName = "Negation"

class Negation(
    categories: List<NegationValue>,
    affected: Set<PSpeechPart>,
    staticSpeechParts: Set<SpeechPart>
) : AbstractChangeCategory(
    categories,
    NegationValue.values().toSet(),
    affected,
    staticSpeechParts,
    outName
)

object NegationRandomSupplements : CategoryRandomSupplements {
    override fun realizationTypeProbability(categoryRealization: CategoryRealization) = when (categoryRealization) {
        //TODO not actual data
        CategoryRealization.PrefixSeparateWord -> 502.0
        CategoryRealization.SuffixSeparateWord -> 502.0
        CategoryRealization.Prefix -> 395.0
        CategoryRealization.Suffix -> 395.0
        CategoryRealization.Reduplication -> 0.0
        CategoryRealization.Passing -> 0.0
        CategoryRealization.NewWord -> 0.0
    }

    override fun speechPartProbabilities(speechPart: SpeechPart) = when (speechPart) {
        Noun -> listOf()
        Verb -> listOf(SourceTemplate(SelfStated, 100.0))
        Adjective -> listOf()//TODO not an actual data
        Adverb -> listOf()
        Numeral -> listOf()
        Article -> listOf()
        PersonalPronoun -> listOf()
        DeixisPronoun -> listOf()
        Particle -> listOf()
        Adposition -> listOf()
    }

    override fun specialRealization(values: List<CategoryValue>, speechPart: SpeechPart): Set<RealizationBox> {
        return emptyRealization
    }

    override fun randomRealization() = NegationPresence.values().randomElement().possibilities

    override fun randomIsCompulsory(speechPart: SpeechPart) = false
}

enum class NegationPresence(override val probability: Double, val possibilities: List<NegationValue>) : SampleSpaceObject {
    Default(1.0, listOf(NegationValue.Negative))
}

enum class NegationValue(override val semanticsCore: SemanticsCore, override val shortName: String) : CategoryValue {
    Negative(SemanticsCore("(negation indicator)".toCluster(), Particle.toUnspecified()), "NEG");

    override val parentClassName = outName
}
