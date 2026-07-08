package io.tashtabash.lang.language.syntax.clause.construction

import io.tashtabash.lang.language.Language
import io.tashtabash.lang.language.category.MoodValue
import io.tashtabash.lang.language.category.moodName
import io.tashtabash.lang.language.syntax.clause.realization.PredicateClause


interface VerbConstruction: Construction {
    val name: String

    fun apply(predicate: PredicateClause, language: Language): PredicateClause
}

data class ApplyMood(val value: MoodValue): VerbConstruction {
    override val name = value.toString()

    override fun apply(predicate: PredicateClause, language: Language): PredicateClause {
        val potentialValue = language.changeParadigm.wordChangeParadigm
            .getParadigm(predicate.head.semanticsCore.speechPart)
            .getCategory(moodName)[value]
        val categories = predicate.additionalCategories.filter { it.categoryValue !is MoodValue } +
                potentialValue

        return predicate.withCategories(categories)
    }
}
