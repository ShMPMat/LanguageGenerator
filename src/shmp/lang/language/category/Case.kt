package shmp.lang.language.category

import shmp.lang.language.AbstractCategoryValue
import shmp.lang.language.CategoryRealization
import shmp.lang.language.CategoryValues
import shmp.lang.language.category.CaseValue.*
import shmp.lang.language.category.CategorySource.Agreement
import shmp.lang.language.category.CategorySource.Self
import shmp.lang.language.category.paradigm.SourcedCategory
import shmp.lang.language.category.paradigm.withCoCategories
import shmp.lang.language.lexis.Meaning
import shmp.lang.language.lexis.SpeechPart
import shmp.lang.language.lexis.SpeechPart.*
import shmp.lang.language.lexis.nominals
import shmp.lang.language.syntax.SyntaxRelation.Agent
import shmp.lang.language.syntax.SyntaxRelation.Nominal
import shmp.lang.utils.values
import shmp.lang.utils.valuesSet
import shmp.random.SampleSpaceObject
import shmp.random.singleton.RandomSingleton
import shmp.random.singleton.chanceOf
import shmp.random.singleton.randomElement
import shmp.random.singleton.testProbability


const val caseName = "Case"
const val adpositionName = "Adposition"

class Case(
    categories: List<CaseValue>,
    affected: Set<PSpeechPart>,
    staticSpeechParts: Set<SpeechPart>,
    outType: String = caseName
) : AbstractChangeCategory(
    categories,
    CaseValue::class.valuesSet(),
    affected,
    staticSpeechParts,
    outType
)

class CaseRandomSupplements : CategoryRandomSupplements {
    override fun realizationTypeProbability(categoryRealization: CategoryRealization): Double =
        when (categoryRealization) {//TODO not actual data
            CategoryRealization.PrefixSeparateWord -> 20.0
            CategoryRealization.SuffixSeparateWord -> 20.0
            CategoryRealization.Prefix -> 100.0
            CategoryRealization.Suffix -> 100.0
            CategoryRealization.Reduplication -> 0.0
            CategoryRealization.Passing -> 0.0
            CategoryRealization.NewWord -> 0.0
        }

    private val nounProbability = RandomSingleton.random.nextDouble(90.0, 100.0)
    private val pronounProbability = RandomSingleton.random.nextDouble(90.0, 100.0)

    override fun speechPartProbabilities(speechPart: SpeechPart) = when (speechPart) {
        Noun -> listOf(SourceTemplate(Self, nounProbability))
        Verb -> listOf()
        Adjective -> listOf(SourceTemplate(Agreement(Nominal, nominals), 80.0))
        Adverb -> listOf()
        Numeral -> listOf(SourceTemplate(Agreement(Nominal, nominals), 80.0))
        Article -> listOf(SourceTemplate(Agreement(Agent, nominals), 1.0))
        PersonalPronoun -> listOf(SourceTemplate(Self, pronounProbability))
        DeixisPronoun -> listOf(SourceTemplate(Self, 90.0))
        Adposition -> listOf()
        Particle -> listOf()
    }

    override fun specialRealization(
        values: CategoryValues,
        speechPart: SpeechPart,
        categories: List<SourcedCategory>
    ): Set<RealizationBox> {
        val acceptableValues = values.filter { it.parentClassName == nounClassName }

        val defaultRealization = if (nonCoreCases.all { it !in acceptableValues }) setOf(
            RealizationBox(CategoryRealization.Suffix, 1.5),
            RealizationBox(CategoryRealization.Prefix, 1.5)
        ) else emptyRealization

        return when (speechPart) {
            PersonalPronoun -> setOf(//TODO no actual data
                noValue(1.0),
                RealizationBox(CategoryRealization.NewWord, 2.0)
            )
            DeixisPronoun -> setOf(//TODO no actual data
                RealizationBox(CategoryRealization.Suffix, 1.5),
                RealizationBox(CategoryRealization.Prefix, 1.5)
            )
            else -> defaultRealization
        }
    }

    override fun randomRealization(): List<CaseValue> {
        val coreCases = CoreCasePresence.values().randomElement().possibilities.toMutableList()

        if (coreCases.isNotEmpty())
            0.3.chanceOf { coreCases.add(Topic) }

        val nonCoreCases = if (coreCases.isEmpty())
            0.25.chanceOf<List<CaseValue>> {
                NonCoreCasePresence.ObliqueOnly.possibilities
            } ?: listOf()
        else
            NonCoreCasePresence.values().randomElement().possibilities

        return coreCases + nonCoreCases
    }

    override fun randomIsCompulsory(speechPart: SpeechPart) = when (speechPart) {
        Noun -> true
        Adjective -> 0.95.testProbability()
        Article -> 0.9.testProbability()
        Numeral -> 0.9.testProbability()
        PersonalPronoun -> 0.8.testProbability()
        DeixisPronoun -> 0.8.testProbability()
        else -> true
    } withCoCategories listOf()
}

object AdpositionRandomSupplements : CategoryRandomSupplements {
    override fun realizationTypeProbability(categoryRealization: CategoryRealization): Double =
        when (categoryRealization) {//TODO not actual data
            CategoryRealization.PrefixSeparateWord -> 20.0
            CategoryRealization.SuffixSeparateWord -> 20.0
            CategoryRealization.Prefix -> 0.0
            CategoryRealization.Suffix -> 0.0
            CategoryRealization.Reduplication -> 0.0
            CategoryRealization.Passing -> 0.0
            CategoryRealization.NewWord -> 0.0
        }

    override fun speechPartProbabilities(speechPart: SpeechPart) =
        CaseRandomSupplements().speechPartProbabilities(speechPart)
            .mapNotNull { if (it.source == Self) it.copy(probability = 100.0) else null }

    override fun specialRealization(
        values: CategoryValues,
        speechPart: SpeechPart,
        categories: List<SourcedCategory>
    ) = emptyRealization

    override fun randomRealization(): List<CaseValue> = emptyList()

    override fun randomIsCompulsory(speechPart: SpeechPart) = false withCoCategories listOf()
}


enum class NonCoreCasePresence(override val probability: Double, val possibilities: List<CaseValue>) :
    SampleSpaceObject {
    None(100.0, listOf()),
    All(100.0, listOf(Genitive, Dative, Instrumental, Locative)),
    ObliqueOnly(25.0, listOf(Oblique))
}

enum class CoreCasePresence(override val probability: Double, val possibilities: List<CaseValue>) : SampleSpaceObject {
    None(145.0, listOf()),
    NA(145.0, listOf(Nominative, Accusative)),
    NAEA(145.0, listOf(Nominative, Accusative, Ergative, Absolutive)),
    NAE(145.0, listOf(Nominative, Accusative, Ergative)),
    AE(5.0, listOf(Ergative, Absolutive)),
}

sealed class CaseValue(meaning: Meaning, alias: String) : AbstractCategoryValue(caseName, meaning, alias, Adposition) {
    object Nominative : CaseValue("(nominative case ind)", "NOM")
    object Accusative : CaseValue("(accusative case ind)", "ACC")
    object Ergative : CaseValue("(ergative case ind)", "ERG")
    object Absolutive : CaseValue("(absolutive case ind)", "ABS")
    object Topic : CaseValue("(topic case ind)", "TOP")
    object Oblique : CaseValue("(oblique case ind)", "OBL")
    object Genitive : CaseValue("(genitive case ind)", "GEN")
    object Dative : CaseValue("(dative case ind)", "DAT")
    object Instrumental : CaseValue("(instrumental case ind)", "INS")
    object Locative : CaseValue("(locative case ind)", "LOC")
}

val coreCases = listOf(Nominative, Accusative, Ergative, Absolutive)

val nonCoreCases = CaseValue::class.values().filter { it !in coreCases && it != Oblique }
