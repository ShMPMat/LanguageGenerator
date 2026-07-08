package io.tashtabash.lang.language.syntax.clause.construction

import io.tashtabash.lang.language.Language
import io.tashtabash.lang.language.category.MoodValue
import io.tashtabash.lang.language.syntax.clause.realization.AdverbClause
import io.tashtabash.lang.language.syntax.clause.realization.UnfoldableClause
import io.tashtabash.lang.language.syntax.clause.realization.VerbSentenceClause


abstract class PotentialConstruction : Construction {
    abstract fun apply(sentence: VerbSentenceClause, language: Language): UnfoldableClause

    override fun toString(): String = this.javaClass.simpleName + " potential construction"

    object Mood : PotentialConstruction() {
        override fun apply(sentence: VerbSentenceClause, language: Language): UnfoldableClause =
            sentence.copy(
                predicate = ApplyMood(MoodValue.Potential).apply(sentence.predicate, language)
            )
    }

    object Adverb : PotentialConstruction() {
        override fun apply(sentence: VerbSentenceClause, language: Language): UnfoldableClause {
            val adverb = language.lexis.getFunctionWord(this)

            return sentence.copy(
                predicate = sentence.predicate.addAdjunct(AdverbClause(adverb))
            )
        }
    }

    data class Auxiliary(val construction: AuxiliaryConstruction) : PotentialConstruction() {
        override fun apply(sentence: VerbSentenceClause, language: Language): UnfoldableClause =
            sentence.copy(predicate = construction.apply(sentence.predicate, language))

        override fun toString() = construction.toString()
    }
}
