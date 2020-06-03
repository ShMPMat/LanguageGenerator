package shmp.language.category

import shmp.language.*
import shmp.language.category.DefinitenessValue.*
import shmp.language.syntax.SyntaxRelation
import shmp.random.SampleSpaceObject
import shmp.random.randomElement
import kotlin.random.Random

const val definitenessName = "Definiteness"

class Definiteness(
    categories: List<DefinitenessValue>,
    override val affectedSpeechParts: Set<SpeechPart>,
    override val staticSpeechParts: Set<SpeechPart>
) : AbstractChangeCategory(
    categories,
    values().toSet(),
    definitenessName
)

object DefinitenessRandomSupplements : CategoryRandomSupplements {
    override fun realizationTypeProbability(categoryRealization: CategoryRealization): Double =
        when (categoryRealization) {//TODO not an actual data
            CategoryRealization.PrefixSeparateWord -> 400.0
            CategoryRealization.SuffixSeparateWord -> 20.0
            CategoryRealization.Prefix -> 100.0
            CategoryRealization.Suffix -> 30.0
            CategoryRealization.Reduplication -> 0.0
            CategoryRealization.Passing -> 0.0
            CategoryRealization.NewWord -> 0.0
        }

    override fun speechPartProbabilities(speechPart: SpeechPart) =
        when (speechPart) {
            SpeechPart.Noun -> 500.0
            SpeechPart.Verb -> 0.0
            SpeechPart.Adjective -> 100.0
            SpeechPart.Adverb -> 0.0
            SpeechPart.Numeral -> 0.0
            SpeechPart.Article -> 0.0
            SpeechPart.Pronoun -> 0.0
            SpeechPart.Particle -> 0.0
        }

    override fun speechPartCategorySource(speechPart: SpeechPart) =
        when (speechPart) {
            SpeechPart.Noun -> CategorySource.SelfStated
            SpeechPart.Verb -> null
            SpeechPart.Adjective -> CategorySource.RelationGranted(SyntaxRelation.Subject)
            SpeechPart.Adverb -> null
            SpeechPart.Numeral -> null
            SpeechPart.Article -> null
            SpeechPart.Pronoun -> null
            SpeechPart.Particle -> null
        }

    override fun specialRealization(values: List<CategoryValue>, speechPart: SpeechPart): Set<RealizationBox> {
        val acceptableValues = values.filter { it.parentClassName == definitenessName }
        if (acceptableValues.size != 1) return emptyRealization
        val value = values.first()
        return when(value) {
            None -> setOf(RealizationBox(CategoryRealization.Passing, 1.0))
            else -> emptyRealization
        }
    }

    override fun randomRealization(random: Random) = randomElement(
        DefinitenessPresence.values(),
        random
    ).presentDefiniteness
}

enum class DefinitenessPresence(
    override val probability: Double,
    val presentDefiniteness: List<DefinitenessValue>
) : SampleSpaceObject {
    NoDefiniteness(198.0, listOf()),
    OnlyDefinite(98.0, listOf(None, Definite)),
    OnlyIndefinite(45.0, listOf(None, Indefinite)),
    DefiniteAndIndefinite(209.0, listOf(None, Definite, Indefinite))
}

enum class DefinitenessValue(override val semanticsCore: SemanticsCore) : CategoryValue {
    //TODO there are proper and partitive articles, naniiiiii???
    None(SemanticsCore("", SpeechPart.Article, setOf())),
    Definite(SemanticsCore("the", SpeechPart.Article, setOf())),
    Indefinite(SemanticsCore("a", SpeechPart.Article, setOf()));

    override val parentClassName = definitenessName
}