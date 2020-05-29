package shmp.language.category

import shmp.language.*
import shmp.random.SampleSpaceObject
import shmp.random.randomElement
import kotlin.random.Random

private const val outName = "Animosity"

class Animosity(
    categories: List<AnimosityValue>,
    override val affectedSpeechParts: Set<SpeechPart>,
    override val staticSpeechParts: Set<SpeechPart>
) : AbstractChangeCategory(
    categories,
    AnimosityValue.values().toSet(),
    outName,
    "Has no animosity"
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

    override fun speechPartProbabilities(speechPart: SpeechPart): Double =
        when (speechPart) {
            SpeechPart.Noun -> 100.0//TODO no data at all
            SpeechPart.Verb -> 10.0
            SpeechPart.Adjective -> 10.0
            SpeechPart.Adverb -> 0.0
            SpeechPart.Numeral -> 0.0
            SpeechPart.Article -> 5.0
            SpeechPart.Pronoun -> 5.0
            SpeechPart.Particle -> 0.0
        }

    override fun specialRealization(values: List<CategoryValue>, speechPart: SpeechPart): Set<RealizationBox> {
        val acceptableValues = values.filter { it.parentClassName == outName }
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

    override fun randomRealization(random: Random) = randomElement(
        AnimosityPresence.values(),
        random
    ).presentAnimosity

    override fun randomStaticSpeechParts(random: Random) = setOf(SpeechPart.Noun)
}

enum class AnimosityPresence(
    override val probability: Double,
    val presentAnimosity: List<AnimosityValue>
) : SampleSpaceObject {
    NoAnimosity(100.0, listOf()),
    SimpleAnimisity(100.0, listOf(AnimosityValue.Animate, AnimosityValue.Inanimate))
}

enum class AnimosityValue(override val syntaxCore: SyntaxCore) : CategoryValue {
    Animate(SyntaxCore("animate indicator", SpeechPart.Particle, setOf())),
    Inanimate(SyntaxCore("inanimate indicator", SpeechPart.Particle, setOf()));

    override val parentClassName = outName
}