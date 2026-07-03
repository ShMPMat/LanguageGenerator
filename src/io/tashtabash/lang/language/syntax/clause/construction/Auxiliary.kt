package io.tashtabash.lang.language.syntax.clause.construction

import io.tashtabash.lang.language.Language
import io.tashtabash.lang.language.LanguageException
import io.tashtabash.lang.language.category.CategorySource
import io.tashtabash.lang.language.category.value.CategoryValues
import io.tashtabash.lang.language.derivation.DerivationType
import io.tashtabash.lang.language.syntax.arranger.Arranger
import io.tashtabash.lang.language.syntax.clause.realization.AuxVerbClause
import io.tashtabash.lang.language.syntax.clause.realization.PredicateClause
import io.tashtabash.random.singleton.randomElement


interface AuxiliaryConstruction: Construction {
    val name: String

    fun apply(predicate: PredicateClause, language: Language): PredicateClause
}


data class SerialAuxiliary(val arranger: Arranger, override val name: String = defaultAuxName) : AuxiliaryConstruction {
    override fun apply(predicate: PredicateClause, language: Language): PredicateClause {
        val aux = language.lexis.getFunctionWord(this)

        return AuxVerbClause(aux, predicate, predicate.additionalCategories, arranger)
    }

    override fun toString() = "Auxiliary verb ${if (name == defaultAuxName) "" else "\"$name\""} used" +
            " in a serial construction $arranger"
}

//Governed categories are Self-only and override already existing values
data class Auxiliary(
    val arranger: Arranger,
    val governedCategories: CategoryValues,
    val governedDerivation: DerivationType? = null,
    override val name: String = defaultAuxName
) : AuxiliaryConstruction {
    override fun apply(predicate: PredicateClause, language: Language): PredicateClause {
        val aux = language.lexis.getFunctionWord(this)
        val resultGovernedCategories = predicate.additionalCategories
            .map { c ->
                if (c.source != CategorySource.Self)
                    return@map c
                val newC = c.parent.actualSourcedValues.firstOrNull { it.categoryValue in governedCategories }

                newC ?: c
            }
        val governedWord =
            if (governedDerivation != null)
                predicate.head.semanticsCore.derivationCluster.typeToCore
                    .getValue(governedDerivation)
                    .randomElement()
                    .value
                    ?.let { language.lexis.getWordOrNull(it) }
                    ?: throw LanguageException("'${predicate.head}' must have the derivation $governedDerivation")
            else
                predicate.head

        return AuxVerbClause(
            aux,
            predicate.withWord(governedWord)
                .withCategories(resultGovernedCategories),
            predicate.additionalCategories,
            arranger
        )
    }

    private val governmentString: String
        get() = governedCategories.joinToString(", ") +
                if (governedDerivation != null )
                    " and $governedDerivation"
                else ""

    override fun toString() = "Auxiliary verb ${if (name == defaultAuxName) "" else "\"$name\""} governing " +
            "$governmentString, $arranger"
}

const val defaultAuxName = "<none>"
