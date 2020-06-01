package shmp.language.category

import shmp.language.*
import shmp.language.category.TenseValue.*
import shmp.random.SampleSpaceObject
import shmp.random.randomElement
import kotlin.random.Random

private const val outName = "Tense"

class Tense(
    categories: List<TenseValue>,
    override val affectedSpeechParts: Set<SpeechPart>,
    override val staticSpeechParts: Set<SpeechPart>
) : AbstractChangeCategory(
    categories,
    values().toSet(),
    outName
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

    override fun randomRealization(random: Random) = randomElement(
        TensePresence.values(),
        random
    ).possibilities
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

enum class TenseValue(override val semanticsCore: SemanticsCore) : CategoryValue {
    Present(SemanticsCore("(present tense indicator)", SpeechPart.Particle, setOf())),
    Future(SemanticsCore("(future tense indicator)", SpeechPart.Particle, setOf())),
    Past(SemanticsCore("(past tense indicator)", SpeechPart.Particle, setOf())),
    DayPast(SemanticsCore("(day past tense indicator)", SpeechPart.Particle, setOf())),
    SomeDaysPast(SemanticsCore("(some days past tense indicator)", SpeechPart.Particle, setOf())),
    MonthPast(SemanticsCore("(month past tense indicator)", SpeechPart.Particle, setOf())),
    YearPast(SemanticsCore("(year past tense indicator)", SpeechPart.Particle, setOf()));

    override val parentClassName = outName
}