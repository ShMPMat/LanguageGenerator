package shmp.language.categories

import shmp.language.*
import shmp.random.SampleSpaceObject

private const val outName = "Numbers"

class Numbers(
    categories: List<NumbersValue>,
    override val affectedSpeechParts: Set<SpeechPart>
) : AbstractChangeCategory(
    categories,
    NumbersValue.values().toSet(),
    outName,
    "Has no numbers"
)

object NumbersRandomSupplements : CategoryRandomSupplements {
    override val mainSpeechPart: SpeechPart = SpeechPart.Noun

    override fun realizationTypeProbability(categoryRealization: CategoryRealization): Double =
        when (categoryRealization) {//TODO not actual data
            CategoryRealization.PrefixSeparateWord -> 10.0
            CategoryRealization.SuffixSeparateWord -> 10.0
            CategoryRealization.Prefix -> 100.0
            CategoryRealization.Suffix -> 100.0
            CategoryRealization.Reduplication -> 0.0
        }

    override fun speechPartProbabilities(speechPart: SpeechPart): Double =
        when (speechPart) {
            SpeechPart.Noun -> 100.0
            SpeechPart.Verb -> 100.0
            SpeechPart.Adjective -> 100.0
            SpeechPart.Adverb -> 0.0
            SpeechPart.Numeral -> 0.0
            SpeechPart.Article -> 10.0
            SpeechPart.Pronoun -> 50.0
            SpeechPart.Particle -> 0.0
        }
}

enum class NumbersPresence(override val probability: Double, val presentNumbers: List<NumbersValue>) :
    SampleSpaceObject {
    None(100.0, listOf()),
    Plural(180.0, listOf(NumbersValue.Singular, NumbersValue.Plural)),
    Dual(20.0, listOf(NumbersValue.Singular, NumbersValue.Dual, NumbersValue.Plural))
}

enum class NumbersValue(override val syntaxCore: SyntaxCore) : CategoryValue {
    Singular(SyntaxCore("(singular number indicator)", SpeechPart.Particle, setOf())),//TODO more diversity
    Dual(SyntaxCore("(dual number indicator)", SpeechPart.Particle, setOf())),
    Plural(SyntaxCore("(plural number indicator)", SpeechPart.Particle, setOf()));

    override val parentClassName = outName
}