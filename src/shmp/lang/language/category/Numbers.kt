package shmp.lang.language.category

import shmp.lang.language.CategoryRealization
import shmp.lang.language.CategoryRealization.*
import shmp.lang.language.CategoryValue
import shmp.lang.language.lexis.SpeechPart.*
import shmp.lang.language.category.CategorySource.*
import shmp.lang.language.category.NumbersValue.*
import shmp.lang.language.lexis.*
import shmp.lang.language.syntax.SyntaxRelation
import shmp.random.SampleSpaceObject
import shmp.random.singleton.randomElement
import shmp.random.singleton.testProbability


private const val outName = "Numbers"

class Numbers(
    categories: List<NumbersValue>,
    affected: Set<PSpeechPart>,
    staticSpeechParts: Set<SpeechPart>
) : AbstractChangeCategory(
    categories,
    NumbersValue.values().toSet(),
    affected,
    staticSpeechParts,
    outName
)

object NumbersRandomSupplements : CategoryRandomSupplements {
    override fun realizationTypeProbability(categoryRealization: CategoryRealization) = when (categoryRealization) {
        //TODO not actual data
        PrefixSeparateWord -> 10.0
        SuffixSeparateWord -> 10.0
        Prefix -> 100.0
        Suffix -> 100.0
        Reduplication -> 0.0
        Passing -> 0.0
        NewWord -> 0.0
    }

    override fun speechPartProbabilities(speechPart: SpeechPart) = when (speechPart) {
        Noun -> listOf(SourceTemplate(SelfStated, 100.0))
        Verb -> listOf(
            SourceTemplate(RelationGranted(SyntaxRelation.Agent), 99.0),
            SourceTemplate(RelationGranted(SyntaxRelation.Patient), 5.0)
        )
        Adjective -> listOf(SourceTemplate(RelationGranted(SyntaxRelation.Agent), 99.0))
        Adverb -> listOf()
        Numeral -> listOf()
        Article -> listOf(SourceTemplate(RelationGranted(SyntaxRelation.Agent), 10.0))
        PersonalPronoun -> listOf(SourceTemplate(SelfStated, 99.0))
        DeixisPronoun -> listOf(SourceTemplate(SelfStated, 99.0))
        Particle -> listOf()
        Adposition -> listOf(SourceTemplate(RelationGranted(SyntaxRelation.Agent), 2.0))
    }

    override fun specialRealization(values: List<CategoryValue>, speechPart: SpeechPart): Set<RealizationBox> {
        val acceptableValues = values.filter { it.parentClassName == outName }
        if (acceptableValues.size != 1) return emptyRealization
        return when (values.first()) {
            Singular -> setOf(
                noValue(1.0),
                RealizationBox(Passing, 1.0)
            )
            Plural -> when (speechPart) {
                in setOf(Noun, PersonalPronoun) -> setOf(
                    noValue(1.0),
                    RealizationBox(Reduplication, 1.0)
                )
                else -> emptyRealization
            }
            else -> when (speechPart) {
                PersonalPronoun -> setOf(//TODO no actual data
                    noValue(1.0),
                    RealizationBox(NewWord, 1.3)
                )
                DeixisPronoun -> setOf(//TODO no actual data
                    RealizationBox(Suffix, 1.5),
                    RealizationBox(Prefix, 1.5)
                )
                else -> emptyRealization
            }
        }
    }

    override fun randomRealization() = NumbersPresence.values().randomElement().possibilities

    override fun randomIsCompulsory(speechPart: SpeechPart) = when (speechPart) {
        Noun -> 0.95.testProbability()
        Verb -> 0.95.testProbability()
        Adjective -> 0.9.testProbability()
        Article -> 0.9.testProbability()
        PersonalPronoun -> 0.9.testProbability()
        DeixisPronoun -> 0.9.testProbability()
        Adposition -> 0.7.testProbability()
        else -> true
    }
}

enum class NumbersPresence(
    override val probability: Double,
    val possibilities: List<NumbersValue>
) : SampleSpaceObject {
    None(100.0, listOf()),
    Plural(180.0, listOf(Singular, NumbersValue.Plural)),
    Dual(20.0, listOf(Singular, NumbersValue.Dual, NumbersValue.Plural))
}

enum class NumbersValue(override val semanticsCore: SemanticsCore, override val shortName: String) : CategoryValue {
    Singular(SemanticsCore("(singular number indicator)".toCluster(), Particle.toUnspecified()), "SN"),//TODO more diversity
    Dual(SemanticsCore("(dual number indicator)".toCluster(), Particle.toUnspecified()), "DL"),
    Plural(SemanticsCore("(plural number indicator)".toCluster(), Particle.toUnspecified()), "PL");

    override val parentClassName = outName
}