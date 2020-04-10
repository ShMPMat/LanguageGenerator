package shmp.language.categories

import shmp.language.*
import shmp.random.SampleSpaceObject

private const val outName = "Tense"

class Tense(
    categories: List<TenseValue>,
    override val affectedSpeechParts: Set<SpeechPart>
) : AbstractChangeCategory(
    categories,
    TenseValue.values().toSet(),
    outName,
    "Has no tense"
)

object TenseRandomSupplements : CategoryRandomSupplements {
    override val mainSpeechPart: SpeechPart = SpeechPart.Verb

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
            SpeechPart.Noun -> 0.0
            SpeechPart.Verb -> 1.0
            SpeechPart.Adjective -> 0.0
            SpeechPart.Adverb -> 0.0
            SpeechPart.Numeral -> 0.0
            SpeechPart.Article -> 0.0
            SpeechPart.Pronoun -> 0.0
            SpeechPart.Particle -> 0.0
        }
}

enum class TensePresence(override val probability: Double, val possibilities: List<TenseValue>): SampleSpaceObject {
    None(6.0, listOf()),//TODO too little actual values
    Future(170.0, listOf(TenseValue.Present, TenseValue.Future)),
    Past(8.0, listOf(TenseValue.Present, TenseValue.Past)),
    PastFuture(180.0, listOf(TenseValue.Present, TenseValue.Past, TenseValue.Future)),
    TwoPast(4.0, listOf(TenseValue.Present, TenseValue.DayPast, TenseValue.Past)),
    TwoPastFuture(43.0, listOf(TenseValue.Present, TenseValue.Past, TenseValue.DayPast, TenseValue.Future)),
    ThreePast(3.0, listOf(TenseValue.Present, TenseValue.Past, TenseValue.SomeDaysPast, TenseValue.DayPast, TenseValue.Past)),
    ThreePastFuture(17.0, listOf(TenseValue.Present, TenseValue.Past, TenseValue.SomeDaysPast, TenseValue.DayPast, TenseValue.Future)),
    FourPast(1.0, listOf(TenseValue.Present, TenseValue.Past, TenseValue.YearPast, TenseValue.SomeDaysPast, TenseValue.DayPast)),
    FourPastFuture(1.0, listOf(TenseValue.Present, TenseValue.Past, TenseValue.YearPast, TenseValue.SomeDaysPast, TenseValue.DayPast, TenseValue.Future)),
    FivePast(1.0, listOf(TenseValue.Present, TenseValue.Past, TenseValue.YearPast, TenseValue.MonthPast, TenseValue.SomeDaysPast, TenseValue.DayPast)),
    FivePastFuture(1.0, listOf(TenseValue.Present, TenseValue.Past, TenseValue.YearPast, TenseValue.MonthPast, TenseValue.SomeDaysPast, TenseValue.DayPast, TenseValue.Future)),
}

enum class TenseValue(override val syntaxCore: SyntaxCore) : CategoryValue {
    Present(SyntaxCore("(present tense indicator)", SpeechPart.Particle, setOf())),
    Future(SyntaxCore("(future tense indicator)", SpeechPart.Particle, setOf())),
    Past(SyntaxCore("(past tense indicator)", SpeechPart.Particle, setOf())),
    DayPast(SyntaxCore("(day past tense indicator)", SpeechPart.Particle, setOf())),
    SomeDaysPast(SyntaxCore("(some days past tense indicator)", SpeechPart.Particle, setOf())),
    MonthPast(SyntaxCore("(month past tense indicator)", SpeechPart.Particle, setOf())),
    YearPast(SyntaxCore("(year past tense indicator)", SpeechPart.Particle, setOf()));

    override val parentClassName = outName
}