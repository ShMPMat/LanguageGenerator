package io.tashtabash.lang.language.syntax.clause.construction

import io.tashtabash.lang.language.Language
import io.tashtabash.lang.language.category.MoodValue
import io.tashtabash.lang.language.category.moodName
import io.tashtabash.lang.language.syntax.clause.realization.AdverbClause
import io.tashtabash.lang.language.syntax.clause.realization.UnfoldableClause
import io.tashtabash.lang.language.syntax.clause.realization.VerbSentenceClause


abstract class PotentialConstruction : Construction {
    abstract fun apply(sentence: VerbSentenceClause, language: Language): UnfoldableClause

    override fun toString(): String = this.javaClass.simpleName + " potential construction"

    object Mood : PotentialConstruction() {
        override fun apply(sentence: VerbSentenceClause, language: Language): UnfoldableClause {
            val potentialValue = language.changeParadigm.wordChangeParadigm
                .getSpeechPartParadigm(sentence.verb.verb.semanticsCore.speechPart)
                .getCategory(moodName)[MoodValue.Potential]
            val categories = sentence.verb.additionalCategories.filter { it.categoryValue !is MoodValue } +
                    potentialValue

            return sentence.copy(
                verb = sentence.verb.copy(additionalCategories = categories)
            )
        }
    }

    object Adverb : PotentialConstruction() {
        override fun apply(sentence: VerbSentenceClause, language: Language): UnfoldableClause {
            val adverb = language.lexis.getFunctionWord(this)

            return sentence.copy(
                verb = sentence.verb.copy(adjuncts = sentence.verb.adjuncts + AdverbClause(adverb))
            )
        }
    }
}
