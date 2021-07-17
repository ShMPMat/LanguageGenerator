package shmp.lang.language.syntax

import shmp.lang.language.NumeralSystemBase


data class NumeralParadigm(val numeralSystemBase: NumeralSystemBase) {
    override fun toString() = """
         |Numeral system base: $numeralSystemBase
         |""".trimMargin()
}
