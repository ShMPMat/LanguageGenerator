package shmp.lang.generator

import shmp.lang.containers.SemanticsCoreTemplate
import shmp.lang.language.NumeralSystemBase
import shmp.lang.language.lexis.Meaning
import shmp.lang.language.lexis.SpeechPart
import shmp.lang.language.syntax.NumeralConstructionType
import shmp.lang.language.syntax.NumeralParadigm
import shmp.random.singleton.randomElement


class NumeralParadigmGenerator {
    private val manyMeaning: Meaning = "Many"

    var numeralTemplates = listOf<SemanticsCoreTemplate>()
        private set

    fun generateNumeralParadigm(): NumeralParadigm {
        val base = NumeralSystemBase.values().randomElement()

        val numeralMeanings = mutableListOf<String>()
        val ranges = mutableMapOf<IntRange, NumeralConstructionType>()
        when(base) {
            NumeralSystemBase.Restricted3 -> {
                numeralMeanings += (1..3).map { it.toString() }
                ranges[1..3] = NumeralConstructionType.SingleWord
                ranges[4..Int.MAX_VALUE] = NumeralConstructionType.SpecialWord(manyMeaning)
            }
            NumeralSystemBase.Restricted5 -> {
                numeralMeanings += (1..5).map { it.toString() }
                ranges[1..5] = NumeralConstructionType.SingleWord
                ranges[6..Int.MAX_VALUE] = NumeralConstructionType.SpecialWord(manyMeaning)
            }
            NumeralSystemBase.Restricted20 -> {
                numeralMeanings += (1..20).map { it.toString() }
                ranges[1..20] = NumeralConstructionType.SingleWord
                ranges[21..Int.MAX_VALUE] = NumeralConstructionType.SpecialWord(manyMeaning)
            }
        }
        numeralMeanings += manyMeaning

        numeralTemplates = numeralMeanings.map { SemanticsCoreTemplate(it, SpeechPart.Numeral) }

        return NumeralParadigm(base, ranges.toList())
    }
}
