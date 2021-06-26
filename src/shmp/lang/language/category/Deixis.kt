package shmp.lang.language.category

import shmp.lang.language.CategoryRealization
import shmp.lang.language.CategoryRealization.*
import shmp.lang.language.CategoryValue
import shmp.lang.language.lexis.SpeechPart.*
import shmp.lang.language.category.CategorySource.SelfStated
import shmp.lang.language.category.DeixisValue.*
import shmp.lang.language.category.paradigm.SourcedCategory
import shmp.lang.language.category.paradigm.withCoCategories
import shmp.lang.language.lexis.*
import shmp.random.SampleSpaceObject
import shmp.random.singleton.randomElement


const val deixisName = "Deixis"

class Deixis(
    categories: List<DeixisValue>,
    affected: Set<PSpeechPart>,
    staticSpeechParts: Set<SpeechPart>
) : AbstractChangeCategory(
    categories,
    DeixisValue.values().toSet(),
    affected,
    staticSpeechParts,
    deixisName
)

object DeixisRandomSupplements : CategoryRandomSupplements {
    override fun realizationTypeProbability(categoryRealization: CategoryRealization) = when (categoryRealization) {
        //TODO not actual data
        PrefixSeparateWord -> 542.0
        SuffixSeparateWord -> 562.0
        Prefix -> 9.0
        Suffix -> 28.0
        Reduplication -> 0.0
        Passing -> 0.0
        NewWord -> 0.0
    }

    override fun speechPartProbabilities(speechPart: SpeechPart) = when (speechPart) {
        Noun -> listOf(SourceTemplate(SelfStated, 100.0))
        Verb -> listOf()
        Adjective -> listOf()
        Adverb -> listOf()
        Numeral -> listOf()
        Article -> listOf()
        PersonalPronoun -> listOf()
        Particle -> listOf()
        DeixisPronoun -> listOf(SourceTemplate(SelfStated, 100.0))
        Adposition -> listOf()
    }

    override fun specialRealization(
        values: List<CategoryValue>,
        speechPart: SpeechPart,
        categories: List<SourcedCategory>
    ): Set<RealizationBox> {
        val acceptableValues = values.filter { it.parentClassName == deixisName }
        if (acceptableValues.size != 1) return emptyRealization
        return when (values.first()) {
            else -> when (speechPart) {
                DeixisPronoun -> setOf(//TODO no actual data
                    RealizationBox(NewWord, 100.0),
                    RealizationBox(Suffix, 50.0),
                    RealizationBox(Prefix, 50.0)
                )
                else -> emptyRealization
            }
        }
    }

    override fun randomRealization() = DeixisPresence.values().randomElement().possibilities

    override fun randomIsCompulsory(speechPart: SpeechPart) = when (speechPart) {
        Noun -> false
        DeixisPronoun -> true
        else -> true
    } withCoCategories listOf()
}

enum class DeixisPresence(
    override val probability: Double,
    val possibilities: List<DeixisValue>
) : SampleSpaceObject {
    Undefined(7.0, listOf(DeixisValue.Undefined)),
    TwoWay(126.0, listOf(Proximal, Distant)),
    ThreeWay(20.0, listOf(Proximal, Medial, Distant)),
    TwoWayAddressee(88.0, listOf(Proximal, ProximalAddressee, Distant)),
    TreeWayAddressee(6.0, listOf(Proximal, ProximalAddressee, Medial, Distant)),
}

enum class DeixisValue(override val semanticsCore: SemanticsCore, override val shortName: String) : CategoryValue {
    Undefined(SemanticsCore("(undefined deixis ind)".toCluster(), DeixisPronoun.toAdnominal()), "UNDEF"),
    Proximal(SemanticsCore("this".toCluster(), DeixisPronoun.toAdnominal()), "PROX"),
    Medial(SemanticsCore("(medial deixis ind)".toCluster(), DeixisPronoun.toAdnominal()), "MED"),
    Distant(SemanticsCore("that".toCluster(), DeixisPronoun.toAdnominal()), "DIST"),
    ProximalAddressee(SemanticsCore("this.addr".toCluster(), DeixisPronoun.toAdnominal()), "PROX.ADDR");
    //TODO more values

    override val parentClassName = deixisName
}