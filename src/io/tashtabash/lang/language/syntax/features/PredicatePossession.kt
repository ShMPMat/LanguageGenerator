package io.tashtabash.lang.language.syntax.features

import io.tashtabash.lang.language.syntax.clause.construction.PredicatePossessionConstruction
import io.tashtabash.random.UnwrappableSSO


data class PredicatePossessionPresence(val predicatePossession: List<UnwrappableSSO<PredicatePossessionConstruction>>) {
    override fun toString() =
        predicatePossession.joinToString { it.value.javaClass.simpleName }
}
