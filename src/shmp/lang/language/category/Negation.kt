package shmp.lang.language.category

import shmp.lang.language.category.value.AbstractCategoryValue
import shmp.lang.language.category.realization.CategoryRealization
import shmp.lang.language.category.value.CategoryValues
import shmp.lang.language.category.CategorySource.Self
import shmp.lang.language.category.paradigm.SourcedCategory
import shmp.lang.language.category.paradigm.withCoCategories
import shmp.lang.language.category.value.RealizationBox
import shmp.lang.language.lexis.Meaning
import shmp.lang.language.lexis.SpeechPart
import shmp.lang.language.lexis.SpeechPart.*
import shmp.lang.utils.valuesSet
import shmp.random.SampleSpaceObject
import shmp.random.singleton.randomElement


private const val negationName = "Negation"

class Negation(
    categories: List<NegationValue>,
    affected: Set<PSpeechPart>,
    staticSpeechParts: Set<SpeechPart>
) : AbstractChangeCategory(
    categories,
    NegationValue::class.valuesSet(),
    affected,
    staticSpeechParts,
    negationName
)

object NegationRandomSupplements : CategoryRandomSupplements {
    override fun realizationTypeProbability(categoryRealization: CategoryRealization) = when (categoryRealization) {
        //TODO not actual data
        CategoryRealization.PrefixWord -> 502.0
        CategoryRealization.SuffixWord -> 502.0
        CategoryRealization.Prefix -> 395.0
        CategoryRealization.Suffix -> 395.0
        CategoryRealization.Reduplication -> 0.0
        CategoryRealization.Passing -> 0.0
        CategoryRealization.Suppletion -> 0.0
    }

    override fun speechPartProbabilities(speechPart: SpeechPart) = when (speechPart) {
        Noun -> listOf()
        Verb -> listOf(SourceTemplate(Self, 100.0))
        Adjective -> listOf()//TODO not an actual data
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
        return emptyRealization
    }

    override fun randomRealization() = NegationPresence.values().randomElement().possibilities

    override fun randomIsCompulsory(speechPart: SpeechPart) = false withCoCategories listOf()
}

enum class NegationPresence(override val probability: Double, val possibilities: List<NegationValue>) :
    SampleSpaceObject {
    Default(1.0, listOf(NegationValue.Negative))
}

sealed class NegationValue(meaning: Meaning, alias: String) : AbstractCategoryValue(negationName, meaning, alias) {
    object Negative : NegationValue("(negation indicator)", "NEG")
}
