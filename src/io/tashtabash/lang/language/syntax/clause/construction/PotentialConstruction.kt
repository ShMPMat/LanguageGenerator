package io.tashtabash.lang.language.syntax.clause.construction

import io.tashtabash.lang.language.Language
import io.tashtabash.lang.language.category.MoodValue
import io.tashtabash.lang.language.category.moodName
import io.tashtabash.lang.language.syntax.arranger.Arranger
import io.tashtabash.lang.language.syntax.clause.realization.AdverbClause
import io.tashtabash.lang.language.syntax.clause.realization.AuxVerbClause
import io.tashtabash.lang.language.syntax.clause.realization.UnfoldableClause
import io.tashtabash.lang.language.syntax.clause.realization.VerbSentenceClause


abstract class PotentialConstruction : Construction {
    abstract fun apply(sentence: VerbSentenceClause, language: Language): UnfoldableClause

    override fun toString(): String = this.javaClass.simpleName + " potential construction"

    object Mood : PotentialConstruction() {
        override fun apply(sentence: VerbSentenceClause, language: Language): UnfoldableClause {
            val potentialValue = language.changeParadigm.wordChangeParadigm
                .getSpeechPartParadigm(sentence.predicate.head.semanticsCore.speechPart)
                .getCategory(moodName)[MoodValue.Potential]
            val categories = sentence.predicate.additionalCategories.filter { it.categoryValue !is MoodValue } +
                    potentialValue

            return sentence.copy(
                predicate = sentence.predicate.withCategories(categories)
            )
        }
    }

    object Adverb : PotentialConstruction() {
        override fun apply(sentence: VerbSentenceClause, language: Language): UnfoldableClause {
            val adverb = language.lexis.getFunctionWord(this)

            return sentence.copy(
                predicate = sentence.predicate.addAdjunct(AdverbClause(adverb))
            )
        }
    }

    data class Auxiliary(val arranger: Arranger) : PotentialConstruction() {
        override fun apply(sentence: VerbSentenceClause, language: Language): UnfoldableClause {
            val aux = language.lexis.getFunctionWord(this)

            return sentence.copy(
                predicate = AuxVerbClause(aux, sentence.predicate, sentence.predicate.additionalCategories, arranger)
            )
        }

        override fun toString() = "Auxiliary verb $arranger"
    }
}
