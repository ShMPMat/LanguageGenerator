package shmp.language.categories

import shmp.language.*
import shmp.language.CategoryRealization.*
import shmp.language.SpeechPart.*
import shmp.language.categories.NumbersValue.*
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
    override val mainSpeechPart: SpeechPart = Noun

    override fun realizationTypeProbability(categoryRealization: CategoryRealization): Double =
        when (categoryRealization) {//TODO not actual data
            PrefixSeparateWord -> 10.0
            SuffixSeparateWord -> 10.0
            Prefix -> 100.0
            Suffix -> 100.0
            Reduplication -> 0.0
            Passing -> 0.0
        }

    override fun speechPartProbabilities(speechPart: SpeechPart): Double =
        when (speechPart) {
            Noun -> 100.0
            Verb -> 100.0
            Adjective -> 100.0
            Adverb -> 0.0
            Numeral -> 0.0
            Article -> 10.0
            Pronoun -> 50.0
            Particle -> 0.0
        }

    override fun specialRealization(values: List<CategoryValue>, speechPart: SpeechPart): Set<RealizationBox> {
        val acceptableValues = values.filter { it.parentClassName == outName }
        if (acceptableValues.size != 1) return emptyRealization
        return when(values.first()) {
            Singular -> setOf(
                noValue(1.0),
                RealizationBox(Passing, 1.0)
            )
            Plural -> when (speechPart) {
                in setOf(Noun, Pronoun) -> setOf(
                    noValue(1.0),
                    RealizationBox(Reduplication, 1.0)
                )
                else -> emptyRealization
            }
            else -> emptyRealization
        }
    }
}

enum class NumbersPresence(override val probability: Double, val possibilities: List<NumbersValue>) :
    SampleSpaceObject {
    None(100.0, listOf()),
    Plural(180.0, listOf(Singular, NumbersValue.Plural)),
    Dual(20.0, listOf(Singular, NumbersValue.Dual, NumbersValue.Plural))
}

enum class NumbersValue(override val syntaxCore: SyntaxCore) : CategoryValue {
    Singular(SyntaxCore("(singular number indicator)", Particle, setOf())),//TODO more diversity
    Dual(SyntaxCore("(dual number indicator)", Particle, setOf())),
    Plural(SyntaxCore("(plural number indicator)", Particle, setOf()));

    override val parentClassName = outName
}