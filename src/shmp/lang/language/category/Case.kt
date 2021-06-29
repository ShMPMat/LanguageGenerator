package shmp.lang.language.category

import shmp.lang.language.CategoryRealization
import shmp.lang.language.CategoryValue
import shmp.lang.language.category.CaseValue.*
import shmp.lang.language.category.CategorySource.*
import shmp.lang.language.category.paradigm.SourcedCategory
import shmp.lang.language.category.paradigm.withCoCategories
import shmp.lang.language.lexis.*
import shmp.lang.language.lexis.SpeechPart.*
import shmp.lang.language.lexis.SpeechPart.Verb
import shmp.lang.language.syntax.SyntaxRelation.*
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
    CaseValue.values().toSet(),
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
        Noun -> listOf(SourceTemplate(SelfStated, nounProbability))
        Verb -> listOf()
        Adjective -> listOf(SourceTemplate(RelationGranted(Nominal, nominals), 80.0))
        Adverb -> listOf()
        Numeral -> listOf()
        Article -> listOf(SourceTemplate(RelationGranted(Agent, nominals), 1.0))
        PersonalPronoun -> listOf(SourceTemplate(SelfStated, pronounProbability))
        DeixisPronoun -> listOf(SourceTemplate(SelfStated, 90.0))
        Adposition -> listOf()
        Particle -> listOf()
    }

    override fun specialRealization(
        values: List<CategoryValue>,
        speechPart: SpeechPart,
        categories: List<SourcedCategory>
    ): Set<RealizationBox> {
        val acceptableValues = values.filter { it.parentClassName == nounClassName }

        val defaultRealization = if (nonCoreCases.all { it !in acceptableValues }) setOf(
            RealizationBox(CategoryRealization.Suffix, 1.5),
            RealizationBox(CategoryRealization.Prefix, 1.5)
        ) else emptyRealization

        return when(speechPart) {
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

        val nonCoreCases = if (coreCases.isEmpty()) {
            0.25.chanceOf<List<CaseValue>> {
                NonCoreCasePresence.ObliqueOnly.possibilities
            } ?: listOf()
        } else {
            NonCoreCasePresence.values().randomElement().possibilities
        }

        return coreCases + nonCoreCases
    }

    override fun randomStaticSpeechParts() = setOf(Noun)

    override fun randomIsCompulsory(speechPart: SpeechPart) = when (speechPart) {
        Noun -> true
        Adjective -> 0.95.testProbability()
        Article -> 0.9.testProbability()
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
            .mapNotNull { if (it.source == SelfStated) it.copy(probability = 100.0) else null }

    override fun specialRealization(
        values: List<CategoryValue>,
        speechPart: SpeechPart,
        categories: List<SourcedCategory>
    ) = emptyRealization

    override fun randomRealization(): List<CaseValue> = emptyList()

    override fun randomStaticSpeechParts() = setOf<SpeechPart>()

    override fun randomIsCompulsory(speechPart: SpeechPart) = false withCoCategories listOf()
}


enum class NonCoreCasePresence(override val probability: Double, val possibilities: List<CaseValue>): SampleSpaceObject {
    None(100.0, listOf()),
    All(100.0, listOf(Genitive, Dative, Instrumental, Locative)),
    ObliqueOnly(25.0, listOf(Oblique))
}

enum class CoreCasePresence(override val probability: Double, val possibilities: List<CaseValue>): SampleSpaceObject {
    None(145.0, listOf()),
    NA(145.0, listOf(Nominative, Accusative)),
    NAEA(145.0, listOf(Nominative, Accusative, Ergative, Absolutive)),
    NAE(145.0, listOf(Nominative, Accusative, Ergative)),
    AE(0.5, listOf(Ergative, Absolutive)),
}

enum class CaseValue(override val semanticsCore: SemanticsCore, override val shortName: String) : CategoryValue {
    Nominative(SemanticsCore("(nominative case ind)".toCluster(), Adposition.toUnspecified()), "NOM"),
    Accusative(SemanticsCore("(accusative case ind)".toCluster(), Adposition.toUnspecified()), "ACC"),
    Ergative(SemanticsCore("(ergative case ind)".toCluster(), Adposition.toUnspecified()), "ERG"),
    Absolutive(SemanticsCore("(absolutive case ind)".toCluster(), Adposition.toUnspecified()), "ABS"),

    Topic(SemanticsCore("(topic case ind)".toCluster(), Adposition.toUnspecified()), "TOP"),

    Oblique(SemanticsCore("(oblique case ind)".toCluster(), Adposition.toUnspecified()), "OBL"),

    Genitive(SemanticsCore("(genitive case ind)".toCluster(), Adposition.toUnspecified()), "GEN"),
    Dative(SemanticsCore("(dative case ind)".toCluster(), Adposition.toUnspecified()), "DAT"),
    Instrumental(SemanticsCore("(instrumental case ind)".toCluster(), Adposition.toUnspecified()), "INS"),
    Locative(SemanticsCore("(locative case ind)".toCluster(), Adposition.toUnspecified()), "LOC");

    override val parentClassName = caseName
}

val coreCases = listOf(Nominative, Accusative, Ergative, Absolutive)

val nonCoreCases = CaseValue.values().filter { it !in coreCases && it != Oblique }