package shmp.language.categories

import shmp.language.*
import shmp.random.SampleSpaceObject

val articlesOutName = "Articles"

class Articles(
    categories: List<CategoryValue>,
    override val affectedSpeechParts: Set<SpeechPart>
) : AbstractChangeCategory(
    categories,
    ArticleValue.values().toSet(),
    articlesOutName,
    "Has no articles"
)

fun CategoryRealization.probabilityForArticle(): Double = when (this) {//TODO not actual data
    CategoryRealization.PrefixSeparateWord -> 400.0
    CategoryRealization.SuffixSeparateWord -> 20.0
    CategoryRealization.Prefix -> 100.0
    CategoryRealization.Suffix -> 30.0
}

fun SpeechPart.probabilityForArticle(): Double = when (this) {
    SpeechPart.Noun -> 0.0
    SpeechPart.Verb -> 0.0
    SpeechPart.Adjective -> 100.0
    SpeechPart.Adverb -> 0.0
    SpeechPart.Numeral -> 0.0
    SpeechPart.Article -> 0.0
    SpeechPart.Pronoun -> 0.0
}

enum class ArticlePresence(override val probability: Double, val presentArticles: List<ArticleValue>) : SampleSpaceObject {
    None(198.0, listOf()),
    Definite(98.0, listOf(ArticleValue.Definite)),
    Indefinite(45.0, listOf(ArticleValue.Indefinite)),
    DefeniteAndIndefenite(209.0, listOf(ArticleValue.Definite, ArticleValue.Indefinite))
}

enum class ArticleValue(override val syntaxCore: SyntaxCore) : CategoryValue {
    Definite(SyntaxCore("the", SpeechPart.Article)),
    Indefinite(SyntaxCore("a", SpeechPart.Article));

    override val parentClassName = articlesOutName
}