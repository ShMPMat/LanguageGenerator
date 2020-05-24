package shmp.language.categories

import shmp.language.*
import shmp.language.categories.ArticleValue.*
import shmp.random.SampleSpaceObject

private const val outName = "Articles"

class Articles(
    categories: List<ArticleValue>,
    override val affectedSpeechParts: Set<SpeechPart>
) : AbstractChangeCategory(
    categories,
    values().toSet(),
    outName,
    "Has no articles"
)

object ArticlesRandomSupplements : CategoryRandomSupplements {
    override fun realizationTypeProbability(categoryRealization: CategoryRealization): Double =
        when (categoryRealization) {//TODO not an actual data
            CategoryRealization.PrefixSeparateWord -> 400.0
            CategoryRealization.SuffixSeparateWord -> 20.0
            CategoryRealization.Prefix -> 100.0
            CategoryRealization.Suffix -> 30.0
            CategoryRealization.Reduplication -> 0.0
            CategoryRealization.Passing -> 0.0
        }

    override fun speechPartProbabilities(speechPart: SpeechPart): Double =
        when (speechPart) {
            SpeechPart.Noun -> 500.0
            SpeechPart.Verb -> 0.0
            SpeechPart.Adjective -> 100.0
            SpeechPart.Adverb -> 0.0
            SpeechPart.Numeral -> 0.0
            SpeechPart.Article -> 10.0//TODO WAT????
            SpeechPart.Pronoun -> 0.0
            SpeechPart.Particle -> 0.0
        }

    override fun specialRealization(values: List<CategoryValue>, speechPart: SpeechPart): Set<RealizationBox> {
        val acceptableValues = values.filter { it.parentClassName == outName }
        if (acceptableValues.size != 1) return emptyRealization
        val value = values.first()
        return when(value) {
            None -> setOf(RealizationBox(CategoryRealization.Passing, 1.0))
            else -> emptyRealization
        }
    }
}

enum class ArticlePresence(override val probability: Double, val presentArticles: List<ArticleValue>) : SampleSpaceObject {
    NoArticle(198.0, listOf()),
    OnlyDefinite(98.0, listOf(None, Definite)),
    OnlyIndefinite(45.0, listOf(None, Indefinite)),
    DefiniteAndIndefinite(209.0, listOf(None, Definite, Indefinite))
}

enum class ArticleValue(override val syntaxCore: SyntaxCore) : CategoryValue {
    //TODO there are proper and partitive articles, naniiiiii???
    None(SyntaxCore("", SpeechPart.Article, setOf())),
    Definite(SyntaxCore("the", SpeechPart.Article, setOf())),
    Indefinite(SyntaxCore("a", SpeechPart.Article, setOf()));

    override val parentClassName = outName
}