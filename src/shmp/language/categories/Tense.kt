package shmp.language.categories

import shmp.language.*
import shmp.language.categories.TenseValue.*
import shmp.random.SampleSpaceObject

private const val outName = "Tense"

class Tense(
    categories: List<TenseValue>,
    override val affectedSpeechParts: Set<SpeechPart>
) : AbstractChangeCategory(
    categories,
    values().toSet(),
    outName,
    "Has no tense"
)

object TenseRandomSupplements : CategoryRandomSupplements {
    override fun realizationTypeProbability(categoryRealization: CategoryRealization) = when (categoryRealization) {
        //TODO not actual data
        CategoryRealization.PrefixSeparateWord -> 10.0
        CategoryRealization.SuffixSeparateWord -> 10.0
        CategoryRealization.Prefix -> 100.0
        CategoryRealization.Suffix -> 100.0
        CategoryRealization.Reduplication -> 0.0
        CategoryRealization.Passing -> 0.0
        CategoryRealization.NewWord -> 0.0
    }

    override fun speechPartProbabilities(speechPart: SpeechPart) = when (speechPart) {
        SpeechPart.Noun -> 0.0
        SpeechPart.Verb -> 100.0
        SpeechPart.Adjective -> 2.0//TODO not an actual data
        SpeechPart.Adverb -> 0.0
        SpeechPart.Numeral -> 0.0
        SpeechPart.Article -> 0.0
        SpeechPart.Pronoun -> 0.0
        SpeechPart.Particle -> 0.0
    }

    override fun specialRealization(values: List<CategoryValue>, speechPart: SpeechPart): Set<RealizationBox> {
        val acceptableValues = values.filter { it.parentClassName == outName }
        if (acceptableValues.size != 1) return emptyRealization
        val value = values.first()
        return when (value) {
            Present -> setOf(
                noValue(1.0),
                RealizationBox(CategoryRealization.Passing, 1.0)
            )
            else -> emptyRealization
        }
    }
}

enum class TensePresence(override val probability: Double, val possibilities: List<TenseValue>) : SampleSpaceObject {
    None(6.0, listOf()),//TODO too little actual values
    OnlyFuture(170.0, listOf(Present, Future)),
    OnlyPast(8.0, listOf(Present, Past)),
    PastFuture(180.0, listOf(Present, Past, Future)),
    TwoPast(4.0, listOf(Present, DayPast, Past)),
    TwoPastFuture(43.0, listOf(Present, Past, DayPast, Future)),
    ThreePast(3.0, listOf(Present, Past, SomeDaysPast, DayPast, Past)),
    ThreePastFuture(17.0, listOf(Present, Past, SomeDaysPast, DayPast, Future)),
    FourPast(1.0, listOf(Present, Past, YearPast, SomeDaysPast, DayPast)),
    FourPastFuture(1.0, listOf(Present, Past, YearPast, SomeDaysPast, DayPast, Future)),
    FivePast(1.0, listOf(Present, Past, YearPast, MonthPast, SomeDaysPast, DayPast)),
    FivePastFuture(1.0, listOf(
            Present,
            Past,
            YearPast,
            MonthPast,
            SomeDaysPast,
            DayPast,
            Future
        )),
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