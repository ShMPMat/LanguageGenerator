package shmp.lang.language.category

import shmp.lang.language.CategoryRealization
import shmp.lang.language.CategoryValue
import shmp.lang.language.category.CategorySource.*
import shmp.lang.language.category.DefinitenessValue.*
import shmp.lang.language.lexis.*
import shmp.lang.language.syntax.SyntaxRelation
import shmp.random.SampleSpaceObject
import shmp.random.singleton.randomElement


const val definitenessName = "Definiteness"

class Definiteness(
    categories: List<DefinitenessValue>,
    affected: Set<PSpeechPart>,
    override val staticSpeechParts: Set<SpeechPart>
) : AbstractChangeCategory(
    categories,
    values().toSet(),
    affected,
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

    override fun speechPartProbabilities(speechPart: SpeechPart) = when (speechPart) {
        SpeechPart.Noun -> listOf(SourceTemplate(SelfStated, 500.0))
        SpeechPart.Verb -> listOf()
        SpeechPart.Adjective -> listOf(SourceTemplate(RelationGranted(SyntaxRelation.Subject), 100.0))
        SpeechPart.Adverb -> listOf()
        SpeechPart.Numeral -> listOf()
        SpeechPart.Article -> listOf()
        SpeechPart.PersonalPronoun -> listOf()
        SpeechPart.DeixisPronoun -> listOf()
        SpeechPart.Particle -> listOf()
    }

    override fun specialRealization(values: List<CategoryValue>, speechPart: SpeechPart): Set<RealizationBox> {
        if (speechPart == SpeechPart.Adjective)
            return setOf(
                RealizationBox(CategoryRealization.Prefix, 1.0),
                RealizationBox(CategoryRealization.Suffix, 1.0)
            )

        val acceptableValues = values.filter { it.parentClassName == definitenessName }
        if (acceptableValues.size != 1) return emptyRealization

        return when(values.first()) {
            None -> setOf(RealizationBox(CategoryRealization.Passing, 1.0))
            else -> emptyRealization
        }
    }

    override fun randomRealization() = DefinitenessPresence.values().randomElement().possibilities
}

enum class DefinitenessPresence(
    override val probability: Double,
    val possibilities: List<DefinitenessValue>
) : SampleSpaceObject {
    NoDefiniteness(198.0, listOf()),
    OnlyDefinite(98.0, listOf(None, Definite)),
    OnlyIndefinite(45.0, listOf(None, Indefinite)),
    DefiniteAndIndefinite(209.0, listOf(None, Definite, Indefinite))
}

enum class DefinitenessValue(override val semanticsCore: SemanticsCore, override val shortName: String) : CategoryValue {
    //TODO there are proper and partitive articles, naniiiiii???
    None(SemanticsCore("".toCluster(), SpeechPart.Article.toUnspecified()), "NONE"),
    Definite(SemanticsCore("the".toCluster(), SpeechPart.Article.toUnspecified()), "DEF"),
    Indefinite(SemanticsCore("a".toCluster(), SpeechPart.Article.toUnspecified()), "INDEF");

    override val parentClassName = definitenessName
}