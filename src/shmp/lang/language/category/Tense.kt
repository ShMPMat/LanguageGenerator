package shmp.lang.language.category

import shmp.lang.language.CategoryRealization
import shmp.lang.language.CategoryValue
import shmp.lang.language.SpeechPart
import shmp.lang.language.SpeechPart.*
import shmp.lang.language.category.CategorySource.*
import shmp.lang.language.category.TenseValue.*
import shmp.lang.language.lexis.MeaningCluster
import shmp.lang.language.lexis.SemanticsCore
import shmp.random.SampleSpaceObject
import shmp.random.randomElement
import kotlin.random.Random

private const val outName = "Tense"

class Tense(
    categories: List<TenseValue>,
    affected: Set<PSpeechPart>,
    override val staticSpeechParts: Set<SpeechPart>
) : AbstractChangeCategory(
    categories,
    TenseValue.values().toSet(),
    affected,
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
        Noun -> listOf()
        Verb -> listOf(SourceTemplate(SelfStated, 100.0))
        Adjective -> listOf(SourceTemplate(SelfStated, 2.0))//TODO not an actual data
        Adverb -> listOf()
        Numeral -> listOf()
        Article -> listOf()
        Pronoun -> listOf()
        Particle -> listOf()
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
    Present(SemanticsCore(MeaningCluster("(present tense indicator)"), Particle, setOf())),
    Future(SemanticsCore(MeaningCluster("(future tense indicator)"), Particle, setOf())),
    Past(SemanticsCore(MeaningCluster("(past tense indicator)"), Particle, setOf())),
    DayPast(SemanticsCore(MeaningCluster("(day past tense indicator)"), Particle, setOf())),
    SomeDaysPast(
        SemanticsCore(
            MeaningCluster("(some days past tense indicator)"),
            Particle,
            setOf()
        )
    ),
    MonthPast(SemanticsCore(MeaningCluster("(month past tense indicator)"), Particle, setOf())),
    YearPast(SemanticsCore(MeaningCluster("(year past tense indicator)"), Particle, setOf()));

    override val parentClassName = outName
}
