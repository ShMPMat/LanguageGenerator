package shmp.lang.language.category

import shmp.lang.language.CategoryRealization
import shmp.lang.language.CategoryValue
import shmp.lang.language.category.CategorySource.*
import shmp.lang.language.lexis.*
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
            SourceTemplate(RelationGranted(SyntaxRelation.Agent), 20.0),
            SourceTemplate(RelationGranted(SyntaxRelation.Patient), 1.0)
        )
        SpeechPart.Adjective -> listOf(SourceTemplate(RelationGranted(SyntaxRelation.Agent), 20.0))
        SpeechPart.Adverb -> listOf()
        SpeechPart.Numeral -> listOf()
        SpeechPart.Article -> listOf(SourceTemplate(RelationGranted(SyntaxRelation.Agent), 10.0))
        SpeechPart.PersonalPronoun -> listOf(SourceTemplate(SelfStated, 10.0))
        SpeechPart.DeixisPronoun -> listOf(SourceTemplate(SelfStated, 10.0))
        SpeechPart.Particle -> listOf()
        SpeechPart.Adposition -> listOf()
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
            else -> return when(speechPart) {
                SpeechPart.PersonalPronoun -> setOf(//TODO no actual data
                    noValue(1.0),
                    RealizationBox(CategoryRealization.NewWord, 2.0)
                )
                SpeechPart.DeixisPronoun -> setOf(//TODO no actual data
                    RealizationBox(CategoryRealization.Suffix, 1.5),
                    RealizationBox(CategoryRealization.Prefix, 1.5)
                )
                else -> emptyRealization
            }
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
    Animate(SemanticsCore("animate indicator".toCluster(), SpeechPart.Particle.toUnspecified()), "ANIM"),
    Inanimate(SemanticsCore("inanimate indicator".toCluster(), SpeechPart.Particle.toUnspecified()), "INANIM");

    override val parentClassName = animosityName
}