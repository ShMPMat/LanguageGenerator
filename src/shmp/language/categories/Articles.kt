package shmp.language.categories

import shmp.language.*

class Articles(
    categories: List<NominalCategoryEnum>
) : AbstractChangeCategory(
    categories,
    ArticleEnum.values().toSet(),
    "Articles",
    "Has no articles"
)

fun NominalCategoryRealization.probabilityForArticle(): Double = when (this) {//TODO not actual data
    NominalCategoryRealization.PrefixSeparateWord -> 400.0
    NominalCategoryRealization.SuffixSeparateWord -> 20.0
    NominalCategoryRealization.Prefix -> 100.0
    NominalCategoryRealization.Suffix -> 30.0
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

enum class ArticlePresence(val probability: Double, val presentArticles: List<ArticleEnum>) {
    None(198.0, listOf()),
    Definite(98.0, listOf(ArticleEnum.Definite)),
    Indefinite(45.0, listOf(ArticleEnum.Indefinite)),
    DefeniteAndIndefenite(209.0, listOf(ArticleEnum.Definite, ArticleEnum.Indefinite))
}

enum class ArticleEnum(override val syntaxCore: SyntaxCore) : NominalCategoryEnum {
    Definite(SyntaxCore("the", SpeechPart.Article)),
    Indefinite(SyntaxCore("a", SpeechPart.Article))
}