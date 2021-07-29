package shmp.lang.language.category

import shmp.lang.language.AbstractCategoryValue
import shmp.lang.language.CategoryRealization
import shmp.lang.language.CategoryValues
import shmp.lang.language.category.CategorySource.Self
import shmp.lang.language.category.TenseValue.*
import shmp.lang.language.category.paradigm.SourcedCategory
import shmp.lang.language.category.paradigm.withCoCategories
import shmp.lang.language.lexis.Meaning
import shmp.lang.language.lexis.SpeechPart
import shmp.lang.language.lexis.SpeechPart.*
import shmp.lang.utils.valuesSet
import shmp.random.SampleSpaceObject
import shmp.random.singleton.randomElement
import shmp.random.singleton.testProbability


const val tenseName = "Tense"

class Tense(
    categories: List<TenseValue>,
    affected: Set<PSpeechPart>,
    staticSpeechParts: Set<SpeechPart>
) : AbstractChangeCategory(
    categories,
    TenseValue::class.valuesSet(),
    affected,
    staticSpeechParts,
    tenseName
)

object TenseRandomSupplements : CategoryRandomSupplements {
    override fun realizationTypeProbability(categoryRealization: CategoryRealization) = when (categoryRealization) {
        //TODO not actual data
        CategoryRealization.PrefixWord -> 10.0
        CategoryRealization.SuffixWord -> 10.0
        CategoryRealization.Prefix -> 100.0
        CategoryRealization.Suffix -> 100.0
        CategoryRealization.Reduplication -> 0.0
        CategoryRealization.Passing -> 0.0
        CategoryRealization.Suppletion -> 0.0
    }

    override fun speechPartProbabilities(speechPart: SpeechPart) = when (speechPart) {
        Noun -> listOf()
        Verb -> listOf(SourceTemplate(Self, 100.0))
        Adjective -> listOf(SourceTemplate(Self, 2.0))//TODO not an actual data
        Adverb -> listOf()
        Numeral -> listOf()
        Article -> listOf()
        PersonalPronoun -> listOf()
        DeixisPronoun -> listOf()
        Particle -> listOf()
        Adposition -> listOf()
    }

    override fun specialRealization(
        values: CategoryValues,
        speechPart: SpeechPart,
        categories: List<SourcedCategory>
    ): Set<RealizationBox> {
        val acceptableValues = values.filter { it.parentClassName == tenseName }
        if (acceptableValues.size != 1) return emptyRealization
        return when (acceptableValues.first()) {
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

sealed class TenseValue(meaning: Meaning, alias: String) : AbstractCategoryValue(tenseName, meaning, alias) {
    object Present : TenseValue("(present tense indicator)", "PRES")
    object Future : TenseValue("(future tense indicator)", "FUT")
    object Past : TenseValue("(past tense indicator)", "PST")
    object DayPast : TenseValue("(day past tense indicator)", "DAY.PST")
    object SomeDaysPast : TenseValue("(some days past tense indicator)", "FEW.DAY.PST")
    object MonthPast : TenseValue("(month past tense ind)", "MTH.PST")
    object YearPast : TenseValue("(year past tense ind)", "YR.PST")
}
