package shmp.language.categories

import shmp.language.*
import shmp.random.SampleSpaceObject
import kotlin.reflect.KClass

val articlesOutName = "Articles"

class Articles(
    categories: List<CategoryEnum>,
    override val affectedSpeechParts: Set<SpeechPart>
) : AbstractChangeCategory(
    categories,
    ArticleEnum.values().toSet(),
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

enum class ArticlePresence(override val probability: Double, val presentArticles: List<ArticleEnum>) : SampleSpaceObject {
    None(198.0, listOf()),
    Definite(98.0, listOf(ArticleEnum.Definite)),
    Indefinite(45.0, listOf(ArticleEnum.Indefinite)),
    DefeniteAndIndefenite(209.0, listOf(ArticleEnum.Definite, ArticleEnum.Indefinite))
}

enum class ArticleEnum(override val syntaxCore: SyntaxCore) : CategoryEnum {
    Definite(SyntaxCore("the", SpeechPart.Article)),
    Indefinite(SyntaxCore("a", SpeechPart.Article));

    override val parentClassName = articlesOutName
}