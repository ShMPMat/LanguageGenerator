package shmp.lang.language.category

import shmp.lang.language.CategoryRealization
import shmp.language.*
import shmp.lang.language.CategoryRealization.*
import shmp.lang.language.CategoryValue
import shmp.lang.language.SpeechPart
import shmp.lang.language.SpeechPart.*
import shmp.lang.language.category.CategorySource.*
import shmp.lang.language.category.NumbersValue.*
import shmp.lang.language.lexis.MeaningCluster
import shmp.lang.language.lexis.SemanticsCore
import shmp.lang.language.syntax.SyntaxRelation
import shmp.random.SampleSpaceObject
import shmp.random.randomElement
import kotlin.random.Random

private const val outName = "Numbers"

class Numbers(
    categories: List<NumbersValue>,
    affected: Set<PSpeechPart>,
    override val staticSpeechParts: Set<SpeechPart>
) : AbstractChangeCategory(
    categories,
    NumbersValue.values().toSet(),
    affected,
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
            SourceTemplate(RelationGranted(SyntaxRelation.Subject), 99.0),
            SourceTemplate(RelationGranted(SyntaxRelation.Object), 5.0)
        )
        Adjective -> listOf(SourceTemplate(RelationGranted(SyntaxRelation.Subject), 99.0))
        Adverb -> listOf()
        Numeral -> listOf()
        Article -> listOf(SourceTemplate(RelationGranted(SyntaxRelation.Subject), 10.0))
        Pronoun -> listOf(SourceTemplate(SelfStated, 99.0))
        Particle -> listOf()
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
                in setOf(Noun, Pronoun) -> setOf(
                    noValue(1.0),
                    RealizationBox(Reduplication, 1.0)
                )
                else -> emptyRealization
            }
            else -> when (speechPart) {
                Pronoun -> setOf(//TODO no actual data
                    noValue(1.0),
                    RealizationBox(NewWord, 1.3)
                )
                else -> emptyRealization
            }
        }
    }

    override fun randomRealization(random: Random) = randomElement(
        NumbersPresence.values(),
        random
    ).possibilities
}

enum class NumbersPresence(
    override val probability: Double,
    val possibilities: List<NumbersValue>
) : SampleSpaceObject {
    None(100.0, listOf()),
    Plural(180.0, listOf(Singular, NumbersValue.Plural)),
    Dual(20.0, listOf(Singular, NumbersValue.Dual, NumbersValue.Plural))
}

enum class NumbersValue(override val semanticsCore: SemanticsCore) : CategoryValue {
    Singular(SemanticsCore(MeaningCluster("(singular number indicator)"), Particle, setOf())),//TODO more diversity
    Dual(SemanticsCore(MeaningCluster("(dual number indicator)"), Particle, setOf())),
    Plural(SemanticsCore(MeaningCluster("(plural number indicator)"), Particle, setOf()));

    override val parentClassName = outName
}