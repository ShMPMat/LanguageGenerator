package shmp.lang.language.category

import shmp.lang.language.CategoryRealization
import shmp.lang.language.CategoryValue
import shmp.lang.language.SpeechPart
import shmp.lang.language.category.CategorySource.*
import shmp.lang.language.lexis.MeaningCluster
import shmp.lang.language.lexis.SemanticsCore
import shmp.lang.language.syntax.SyntaxRelation
import shmp.random.SampleSpaceObject
import shmp.random.singleton.randomElement


const val animosityName = "Animosity"

class Animosity(
    categories: List<AnimosityValue>,
    affected: Set<PSpeechPart>,
    override val staticSpeechParts: Set<SpeechPart>
) : AbstractChangeCategory(
    categories,
    AnimosityValue.values().toSet(),
    affected,
    animosityName
)

object AnimosityRandomSupplements : CategoryRandomSupplements {
    override fun realizationTypeProbability(categoryRealization: CategoryRealization): Double =
        when (categoryRealization) {//TODO not an actual data
            CategoryRealization.PrefixSeparateWord -> 20.0
            CategoryRealization.SuffixSeparateWord -> 20.0
            CategoryRealization.Prefix -> 100.0
            CategoryRealization.Suffix -> 100.0
            CategoryRealization.Reduplication -> 0.0
            CategoryRealization.Passing -> 0.0
            CategoryRealization.NewWord -> 0.0
        }

    override fun speechPartProbabilities(speechPart: SpeechPart) = when (speechPart) {
        SpeechPart.Noun -> listOf(SourceTemplate(SelfStated, 200.0))//TODO no data at all
        SpeechPart.Verb -> listOf(
            SourceTemplate(RelationGranted(SyntaxRelation.Subject), 20.0),
            SourceTemplate(RelationGranted(SyntaxRelation.Object), 1.0)
        )
        SpeechPart.Adjective -> listOf(SourceTemplate(RelationGranted(SyntaxRelation.Subject), 20.0))
        SpeechPart.Adverb -> listOf()
        SpeechPart.Numeral -> listOf()
        SpeechPart.Article -> listOf(SourceTemplate(RelationGranted(SyntaxRelation.Subject), 10.0))
        SpeechPart.PersonalPronoun -> listOf(SourceTemplate(SelfStated, 10.0))
        SpeechPart.DeixisPronoun -> listOf(SourceTemplate(SelfStated, 10.0))
        SpeechPart.Particle -> listOf()
    }

    override fun specialRealization(values: List<CategoryValue>, speechPart: SpeechPart): Set<RealizationBox> {
        val acceptableValues = values.filter { it.parentClassName == animosityName }
        if (acceptableValues.size != 1) return emptyRealization
        val value = values.first()
        return when (value) {
            AnimosityValue.Inanimate -> setOf(
                RealizationBox(CategoryRealization.Passing, 1.0),
                noValue(1.0)
            )
            else -> emptyRealization
        }
    }

    override fun randomRealization() = AnimosityPresence.values().randomElement().possibilities

    override fun randomStaticSpeechParts() = setOf(SpeechPart.Noun)
}

enum class AnimosityPresence(
    override val probability: Double,
    val possibilities: List<AnimosityValue>
) : SampleSpaceObject {
    NoAnimosity(100.0, listOf()),
    SimpleAnimosity(10.0, listOf(AnimosityValue.Animate, AnimosityValue.Inanimate))
}

enum class AnimosityValue(override val semanticsCore: SemanticsCore, override val shortName: String) : CategoryValue {
    Animate(SemanticsCore(MeaningCluster("animate indicator"), SpeechPart.Particle, setOf()), "ANIM"),
    Inanimate(SemanticsCore(MeaningCluster("inanimate indicator"), SpeechPart.Particle, setOf()), "INANIM");

    override val parentClassName = animosityName
}