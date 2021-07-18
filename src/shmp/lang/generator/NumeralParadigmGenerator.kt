package shmp.lang.generator

import shmp.lang.containers.SemanticsCoreTemplate
import shmp.lang.language.NumeralSystemBase
import shmp.lang.language.lexis.SpeechPart
import shmp.lang.language.syntax.NumeralParadigm
import shmp.random.singleton.randomElement


class NumeralParadigmGenerator {
    var numeralTemplates = listOf<SemanticsCoreTemplate>()
        private set

    fun generateNumeralParadigm(): NumeralParadigm {
        val base = NumeralSystemBase.values().randomElement()

        val numeralMeanings = when(base) {
            NumeralSystemBase.Restricted3 -> (1..3).map { it.toString() }
            NumeralSystemBase.Restricted5 -> (1..5).map { it.toString() }
            NumeralSystemBase.Restricted20 -> (1..20).map { it.toString() }
        }

        numeralTemplates = numeralMeanings.map { SemanticsCoreTemplate(it, SpeechPart.Numeral) }

        return NumeralParadigm(base)
    }
}
