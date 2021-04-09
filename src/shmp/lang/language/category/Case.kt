package shmp.lang.language.category

import shmp.lang.language.CategoryRealization
import shmp.lang.language.CategoryValue
import shmp.lang.language.category.CaseValue.*
import shmp.lang.language.category.CategorySource.*
import shmp.lang.language.lexis.*
import shmp.lang.language.lexis.SpeechPart.*
import shmp.lang.language.syntax.SyntaxRelation
import shmp.random.SampleSpaceObject
import shmp.random.singleton.chanceOf
import shmp.random.singleton.randomElement
import shmp.random.singleton.testProbability


private const val outName = "Case"

class Case(
    categories: List<CaseValue>,
    affected: Set<PSpeechPart>,
    override val staticSpeechParts: Set<SpeechPart>
) : AbstractChangeCategory(
    categories,
    CaseValue.values().toSet(),
    affected,
    outName
)

object CaseRandomSupplements : CategoryRandomSupplements {
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

    override fun speechPartProbabilities(speechPart: SpeechPart) = when (speechPart) {
        Noun -> listOf(SourceTemplate(SelfStated, 95.0))
        Verb -> listOf()
        Adjective -> listOf(SourceTemplate(RelationGranted(SyntaxRelation.Agent), 80.0))
        Adverb -> listOf()
        Numeral -> listOf()
        Article -> listOf(SourceTemplate(RelationGranted(SyntaxRelation.Agent), 1.0))
        PersonalPronoun -> listOf(SourceTemplate(SelfStated, 100.0))
        DeixisPronoun -> listOf(SourceTemplate(SelfStated, 90.0))
        Adposition -> listOf()
        Particle -> listOf()
    }

    override fun specialRealization(values: List<CategoryValue>, speechPart: SpeechPart): Set<RealizationBox> {
        val acceptableValues = values.filter { it.parentClassName == nounClassName }
        if (acceptableValues.size != 1) return emptyRealization
        val value = values.first()
        return when(speechPart) {
            PersonalPronoun -> setOf(//TODO no actual data
                noValue(1.0),
                RealizationBox(CategoryRealization.NewWord, 2.0)
            )
            DeixisPronoun -> setOf(//TODO no actual data
                RealizationBox(CategoryRealization.Suffix, 1.5),
                RealizationBox(CategoryRealization.Prefix, 1.5)
            )
            else -> emptyRealization
        }
    }

    override fun randomRealization(): List<CaseValue> {
        val coreCases = CoreCasePresence.values().randomElement().possibilities

        val nonCoreCases = if (coreCases.isEmpty()) {
            0.25.chanceOf<List<CaseValue>> {
                NonCoreCasePresence.ObliqueOnly.possibilities
            } ?: listOf()
        } else {
            NonCoreCasePresence.All.possibilities
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
    }
}


enum class NonCoreCasePresence(override val probability: Double, val possibilities: List<CaseValue>): SampleSpaceObject {
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

    Oblique(SemanticsCore("(oblique case ind)".toCluster(), Adposition.toUnspecified()), "OBL"),

    Genitive(SemanticsCore("(genitive case ind)".toCluster(), Adposition.toUnspecified()), "GEN"),
    Dative(SemanticsCore("(dative case ind)".toCluster(), Adposition.toUnspecified()), "DAT"),
    Instrumental(SemanticsCore("(instrumental case ind)".toCluster(), Adposition.toUnspecified()), "INS"),
    Locative(SemanticsCore("(locative case ind)".toCluster(), Adposition.toUnspecified()), "LOC");

    override val parentClassName = outName
}