package shmp.language.nominal_categories

import shmp.language.*
import shmp.language.nominal_categories.change.CategoryApplicator

class Articles(
    categoryApplicators: Map<NominalCategoryEnum, CategoryApplicator>
) : AbstractChangeNominalCategory(SpeechPart.Noun, categoryApplicators) {
    override fun toString(): String {
        return "Articles:\n" + if (categoryApplicators.isEmpty()) "Has no articles"
        else categoryApplicators.map {
            it.key.toString() + ": " + it.value
        }.joinToString("\n")
    }
}

fun NominalCategoryRealization.probabilityForArticle(): Double = when (this) {//TODO not actual data
    NominalCategoryRealization.PrefixSeparateWord -> 400.0
    NominalCategoryRealization.SuffixSeparateWord -> 20.0
    NominalCategoryRealization.Prefix -> 100.0
    NominalCategoryRealization.Suffix -> 30.0
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