package shmp.language.category

import shmp.language.*
import shmp.language.SpeechPart.*
import shmp.language.category.TenseValue.*
import shmp.language.syntax.SyntaxRelation
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
    TenseValue.values().toSet(),
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
        Noun -> 0.0
        Verb -> 100.0
        Adjective -> 2.0//TODO not an actual data
        Adverb -> 0.0
        Numeral -> 0.0
        Article -> 0.0
        Pronoun -> 0.0
        Particle -> 0.0
    }

    override fun speechPartCategorySource(speechPart: SpeechPart) =
        when (speechPart) {
            Noun -> null
            Verb -> CategorySource.SelfStated
            Adjective -> CategorySource.SelfStated
            Adverb -> null
            Numeral -> null
            Article -> null
            Pronoun -> null
            Particle -> null
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
    Present(SemanticsCore("(present tense indicator)", Particle, setOf())),
    Future(SemanticsCore("(future tense indicator)", Particle, setOf())),
    Past(SemanticsCore("(past tense indicator)", Particle, setOf())),
    DayPast(SemanticsCore("(day past tense indicator)", Particle, setOf())),
    SomeDaysPast(SemanticsCore("(some days past tense indicator)", Particle, setOf())),
    MonthPast(SemanticsCore("(month past tense indicator)", Particle, setOf())),
    YearPast(SemanticsCore("(year past tense indicator)", Particle, setOf()));

    override val parentClassName = outName
}