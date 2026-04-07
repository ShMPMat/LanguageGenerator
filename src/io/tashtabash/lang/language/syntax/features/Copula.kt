package io.tashtabash.lang.language.syntax.features

import io.tashtabash.lang.language.syntax.clause.construction.CopulaConstruction
import io.tashtabash.random.UnwrappableSSO


data class CopulaPresence(val copula: List<UnwrappableSSO<CopulaConstruction>>) {
    override fun toString() =
        copula.joinToString { it.value.javaClass.simpleName }
}
