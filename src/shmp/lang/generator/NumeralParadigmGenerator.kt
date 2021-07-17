package shmp.lang.generator

import shmp.lang.language.NumeralSystemBase
import shmp.lang.language.syntax.NumeralParadigm
import shmp.random.singleton.randomElement


object NumeralParadigmGenerator {
    fun generateNumeralParadigm(): NumeralParadigm {
        val base = NumeralSystemBase.values().randomElement()

        return NumeralParadigm(base)
    }
}
