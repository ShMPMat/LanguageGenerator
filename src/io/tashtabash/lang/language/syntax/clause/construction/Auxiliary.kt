package io.tashtabash.lang.language.syntax.clause.construction

import io.tashtabash.lang.language.Language
import io.tashtabash.lang.language.category.CategorySource
import io.tashtabash.lang.language.category.value.CategoryValues
import io.tashtabash.lang.language.syntax.arranger.Arranger
import io.tashtabash.lang.language.syntax.clause.realization.AuxVerbClause
import io.tashtabash.lang.language.syntax.clause.realization.UnfoldableClause
import io.tashtabash.lang.language.syntax.clause.realization.VerbSentenceClause


interface AuxiliaryConstruction: Construction {
    fun apply(sentence: VerbSentenceClause, language: Language): UnfoldableClause
}


data class SerialAuxiliary(val arranger: Arranger) : AuxiliaryConstruction {
    override fun apply(sentence: VerbSentenceClause, language: Language): UnfoldableClause {
        val aux = language.lexis.getFunctionWord(this)

        return sentence.copy(
            predicate = AuxVerbClause(aux, sentence.predicate, sentence.predicate.additionalCategories, arranger)
        )
    }

    override fun toString() = "Auxiliary verb used in a serial construction $arranger"
}

//Governed categories are Self-only and override already existing values
data class Auxiliary(val arranger: Arranger, val governedCategories: CategoryValues) : AuxiliaryConstruction {
    override fun apply(sentence: VerbSentenceClause, language: Language): UnfoldableClause {
        val aux = language.lexis.getFunctionWord(this)
        val resultGovernedCategories = sentence.predicate.additionalCategories
            .map { c ->
                if (c.source != CategorySource.Self)
                    return@map c
                val newC = c.parent.actualSourcedValues.firstOrNull { it.categoryValue in governedCategories }

                newC ?: c
            }

        return sentence.copy(
            predicate = AuxVerbClause(
                aux,
                sentence.predicate.withCategories(resultGovernedCategories),
                sentence.predicate.additionalCategories,
                arranger
            )
        )
    }

    override fun toString() = "Auxiliary verb governing " + governedCategories.joinToString(", ") + ", $arranger"
}
