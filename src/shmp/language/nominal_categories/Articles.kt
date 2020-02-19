package shmp.language.nominal_categories

import shmp.language.*
import shmp.language.nominal_categories.change.CategoryApplicator

class Articles(
    categories: Set<NominalCategoryEnum>,
    categoryApplicators: Map<SpeechPart, Map<NominalCategoryEnum, CategoryApplicator>>
) : AbstractChangeNominalCategory(
    categories,
    ArticleEnum.values().toSet(),
    categoryApplicators,
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

enum class ArticlePresence(val probability: Double, val presentArticles: Set<ArticleEnum>) {
    None(198.0, setOf()),
    Definite(98.0, setOf(ArticleEnum.Definite)),
    Indefinite(45.0, setOf(ArticleEnum.Indefinite)),
    DefeniteAndIndefenite(209.0, setOf(ArticleEnum.Definite, ArticleEnum.Indefinite))
}

enum class ArticleEnum(override val syntaxCore: SyntaxCore) : NominalCategoryEnum {
    Definite(SyntaxCore("the", SpeechPart.Article)),
    Indefinite(SyntaxCore("a", SpeechPart.Article))
}