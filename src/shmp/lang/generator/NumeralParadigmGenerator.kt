package shmp.lang.generator

import shmp.lang.containers.SemanticsCoreTemplate
import shmp.lang.language.NumeralSystemBase
import shmp.lang.language.lexis.Meaning
import shmp.lang.language.lexis.SpeechPart
import shmp.lang.language.syntax.NumeralConstructionType
import shmp.lang.language.syntax.NumeralParadigm
import shmp.lang.language.syntax.StaticOrder
import shmp.lang.language.syntax.SyntaxRelation
import shmp.lang.language.syntax.SyntaxRelation.*
import shmp.lang.language.syntax.arranger.RelationArranger
import shmp.random.singleton.chanceOf
import shmp.random.singleton.otherwise
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
                0.5.chanceOf {
                    numeralMeanings += (1..20).map { it.toString() }
                    ranges[1..20] = NumeralConstructionType.SingleWord
                } otherwise {
                    numeralMeanings += (1..10).map { it.toString() }
                    numeralMeanings += "20"
                    ranges[1..10] = NumeralConstructionType.SingleWord
                    ranges[20..20] = NumeralConstructionType.SingleWord
                    ranges[11..19] = NumeralConstructionType.AddWord(
                        RelationArranger(StaticOrder(listOf(AdNumeral, SumNumeral, MulNumeral).shuffled())),
                        10
                    )
                }

                ranges[21..Int.MAX_VALUE] = NumeralConstructionType.SpecialWord(manyMeaning)
            }
        }
        numeralMeanings += manyMeaning

        numeralTemplates = numeralMeanings.map { SemanticsCoreTemplate(it, SpeechPart.Numeral) }

        return NumeralParadigm(base, ranges.toList())
    }
}
