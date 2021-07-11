package shmp.lang.language.category

import shmp.lang.language.CategoryRealization
import shmp.lang.language.CategoryValue
import shmp.lang.language.lexis.SpeechPart.*
import shmp.lang.language.category.CategorySource.*
import shmp.lang.language.category.TenseValue.*
import shmp.lang.language.category.paradigm.SourcedCategory
import shmp.lang.language.category.paradigm.withCoCategories
import shmp.lang.language.lexis.*
import shmp.random.SampleSpaceObject
import shmp.random.singleton.randomElement
import shmp.random.singleton.testProbability

const val tenseOutName = "Tense"

class Tense(
    categories: List<TenseValue>,
    affected: Set<PSpeechPart>,
    staticSpeechParts: Set<SpeechPart>
) : AbstractChangeCategory(
    categories,
    TenseValue.values().toSet(),
    affected,
    staticSpeechParts,
    tenseOutName
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
        PersonalPronoun -> listOf()
        DeixisPronoun -> listOf()
        Particle -> listOf()
        Adposition -> listOf()
    }

    override fun specialRealization(
        values: List<CategoryValue>,
        speechPart: SpeechPart,
        categories: List<SourcedCategory>
    ): Set<RealizationBox> {
        val acceptableValues = values.filter { it.parentClassName == tenseOutName }
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

    override fun randomRealization() = TensePresence.values().randomElement().possibilities

    override fun randomIsCompulsory(speechPart: SpeechPart) = when (speechPart) {
        Verb -> true
        Adjective -> 0.5.testProbability()
        else -> true
    } withCoCategories listOf()
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
    FivePastFuture(1.0, listOf(Present, Past, YearPast, MonthPast, SomeDaysPast, DayPast, Future)),
}

enum class TenseValue(override val semanticsCore: SemanticsCore, override val alias: String) : CategoryValue {
    Present(SemanticsCore("(present tense indicator)", Particle.toUnspecified()), "PRES"),
    Future(SemanticsCore("(future tense indicator)", Particle.toUnspecified()), "FUT"),
    Past(SemanticsCore("(past tense indicator)", Particle.toUnspecified()), "PST"),
    DayPast(SemanticsCore("(day past tense indicator)", Particle.toUnspecified()), "DAY.PST"),
    SomeDaysPast(SemanticsCore("(some days past tense indicator)", Particle.toUnspecified()), "FEW.DAY.PST"),
    MonthPast(SemanticsCore("(month past tense ind)", Particle.toUnspecified()), "MTH.PST"),
    YearPast(SemanticsCore("(year past tense ind)", Particle.toUnspecified()), "YR.PST");

    override val parentClassName = tenseOutName
}
