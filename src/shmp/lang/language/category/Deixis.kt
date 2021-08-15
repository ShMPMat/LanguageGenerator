package shmp.lang.language.category

import shmp.lang.language.category.value.AbstractCategoryValue
import shmp.lang.language.category.realization.CategoryRealization
import shmp.lang.language.category.realization.CategoryRealization.*
import shmp.lang.language.category.value.CategoryValues
import shmp.lang.language.category.CategorySource.Self
import shmp.lang.language.category.DeixisValue.*
import shmp.lang.language.category.paradigm.SourcedCategory
import shmp.lang.language.category.paradigm.withCoCategories
import shmp.lang.language.category.value.RealizationBox
import shmp.lang.language.lexis.Meaning
import shmp.lang.language.lexis.SpeechPart
import shmp.lang.language.lexis.SpeechPart.*
import shmp.lang.language.lexis.toAdnominal
import shmp.lang.utils.valuesSet
import shmp.random.SampleSpaceObject
import shmp.random.singleton.randomElement


const val deixisName = "Deixis"

class Deixis(
    categories: List<DeixisValue>,
    affected: Set<PSpeechPart>,
    staticSpeechParts: Set<SpeechPart>
) : AbstractChangeCategory(
    categories,
    DeixisValue::class.valuesSet(),
    affected,
    staticSpeechParts,
    deixisName
)

object DeixisRandomSupplements : CategoryRandomSupplements {
    override fun realizationTypeProbability(categoryRealization: CategoryRealization) = when (categoryRealization) {
        //TODO not actual data
        PrefixWord -> 542.0
        SuffixWord -> 562.0
        Prefix -> 9.0
        Suffix -> 28.0
        Reduplication -> 0.0
        Passing -> 0.0
        Suppletion -> 0.0
    }

    override fun speechPartProbabilities(speechPart: SpeechPart) = when (speechPart) {
        Noun -> listOf(SourceTemplate(Self, 100.0))
        Verb -> listOf()
        Adjective -> listOf()
        Adverb -> listOf()
        Numeral -> listOf()
        Article -> listOf()
        PersonalPronoun -> listOf()
        Particle -> listOf()
        DeixisPronoun -> listOf(SourceTemplate(Self, 100.0))
        Adposition -> listOf()
    }

    override fun specialRealization(
        values: CategoryValues,
        speechPart: SpeechPart,
        categories: List<SourcedCategory>
    ): Set<RealizationBox> {
        val acceptableValues = values.filter { it.parentClassName == deixisName }
        if (acceptableValues.size != 1) return emptyRealization
        return when (acceptableValues.first()) {
            else -> when (speechPart) {
                DeixisPronoun -> setOf(//TODO no actual data
                    RealizationBox(Suppletion, 100.0),
                    RealizationBox(Suffix, 50.0),
                    RealizationBox(Prefix, 50.0)
                )
                else -> emptyRealization
            }
        }
    }

    override fun randomRealization() = DeixisPresence.values().randomElement().possibilities +
            RisePresence.values().randomElement().possibilities

    override fun randomIsCompulsory(speechPart: SpeechPart) = when (speechPart) {
        Noun -> false
        DeixisPronoun -> true
        else -> true
    } withCoCategories listOf()
}

enum class DeixisPresence(override val probability: Double, val possibilities: List<DeixisValue>) : SampleSpaceObject {
    Undefined(7.0, listOf(DeixisValue.Undefined)),
    TwoWay(126.0, listOf(Proximal, Distant)),
    ThreeWay(20.0, listOf(Proximal, Medial, Distant)),
    UnseenTwoWay(10.0, listOf(Proximal, Unseen)),
    UnseenThreeWay(15.0, listOf(Proximal, Medial, Unseen)),
    TwoWayAddressee(88.0, listOf(Proximal, ProximalAddressee, Distant)),
    TreeWayAddressee(6.0, listOf(Proximal, ProximalAddressee, Medial, Distant))
}

enum class RisePresence(override val probability: Double, val possibilities: List<DeixisValue>) : SampleSpaceObject {
    NotPresent(100.0, listOf()),
    Present(5.0, listOf(DistantHigher, DistantLower));
}

sealed class DeixisValue(
    meaning: Meaning,
    alias: String
) : AbstractCategoryValue(deixisName, meaning, alias, DeixisPronoun.toAdnominal()) {
    //TODO more values
    object Undefined : DeixisValue("(undefined deixis ind)", "UNDEF")
    object Proximal : DeixisValue("this", "PROX")
    object Medial : DeixisValue("that.medial", "MED")
    object Distant : DeixisValue("that", "DIST")
    object Unseen : DeixisValue("that", "DIST")
    object ProximalAddressee : DeixisValue("this.addr", "PROX.ADDR")
    object DistantHigher : DeixisValue("that.high", "DIST.HIGH")
    object DistantLower : DeixisValue("that.low", "DIST.LOW")
}
