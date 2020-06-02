package shmp.language.category

import shmp.language.*
import shmp.language.CategoryRealization.*
import shmp.language.SpeechPart.*
import shmp.language.category.NumbersValue.*
import shmp.language.syntax.SyntaxRelation
import shmp.random.SampleSpaceObject
import shmp.random.randomElement
import kotlin.random.Random

private const val outName = "Numbers"

class Numbers(
    categories: List<NumbersValue>,
    override val affectedSpeechParts: Set<SpeechPart>,
    override val staticSpeechParts: Set<SpeechPart>
) : AbstractChangeCategory(
    categories,
    NumbersValue.values().toSet(),
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
        Noun -> 100.0
        Verb -> 99.0
        Adjective -> 99.0
        Adverb -> 0.0
        Numeral -> 0.0
        Article -> 10.0
        Pronoun -> 99.0
        Particle -> 0.0
    }

    override fun speechPartCategorySource(speechPart: SpeechPart) =
        when (speechPart) {
            Noun -> CategorySource.SelfStated()
            Verb -> CategorySource.RelationGranted(SyntaxRelation.Subject)
            Adjective -> CategorySource.RelationGranted(SyntaxRelation.Subject)
            Adverb -> null
            Numeral -> null
            Article -> CategorySource.RelationGranted(SyntaxRelation.Subject)
            Pronoun -> CategorySource.RelationGranted(SyntaxRelation.Subject)
            Particle -> null
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
                    RealizationBox(CategoryRealization.NewWord, 1.3)
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

enum class NumbersPresence(override val probability: Double, val possibilities: List<NumbersValue>) :
    SampleSpaceObject {
    None(100.0, listOf()),
    Plural(180.0, listOf(Singular, NumbersValue.Plural)),
    Dual(20.0, listOf(Singular, NumbersValue.Dual, NumbersValue.Plural))
}

enum class NumbersValue(override val semanticsCore: SemanticsCore) : CategoryValue {
    Singular(SemanticsCore("(singular number indicator)", Particle, setOf())),//TODO more diversity
    Dual(SemanticsCore("(dual number indicator)", Particle, setOf())),
    Plural(SemanticsCore("(plural number indicator)", Particle, setOf()));

    override val parentClassName = outName
}