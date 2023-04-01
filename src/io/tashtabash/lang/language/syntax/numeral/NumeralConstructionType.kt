package io.tashtabash.lang.language.syntax.numeral

import io.tashtabash.lang.language.lexis.Meaning
import io.tashtabash.lang.language.syntax.arranger.Arranger


sealed class NumeralConstructionType {
    object SingleWord : NumeralConstructionType() {
        override fun toString() = "A standalone word"
    }

    data class SpecialWord(val meaning: Meaning) : NumeralConstructionType(){
        override fun toString() = "A word meaning $meaning"
    }

    data class AddWord(val arranger: Arranger, val baseNumber: Int, val oneProb: Double) : NumeralConstructionType() {
        override fun toString() = "Base $baseNumber count, $arranger, one probability is $oneProb"
    }
}
