package shmp.lang.generator

import shmp.lang.containers.SemanticsCoreTemplate
import shmp.lang.language.NumeralSystemBase
import shmp.lang.language.lexis.Meaning
import shmp.lang.language.lexis.SpeechPart
import shmp.lang.language.syntax.NumeralConstructionType
import shmp.lang.language.syntax.NumeralParadigm
import shmp.lang.language.syntax.StaticOrder
import shmp.lang.language.syntax.SyntaxRelation.*
import shmp.lang.language.syntax.arranger.RelationArranger
import shmp.random.singleton.chanceOf
import shmp.random.singleton.otherwise
import shmp.random.singleton.randomElement


class NumeralParadigmGenerator {
    private val manyMeaning: Meaning = "Many"
    private val uniqueTensChance = 0.5

    private val numeralMeanings = mutableListOf<String>()
    private val ranges = mutableMapOf<IntRange, NumeralConstructionType>()
    var numeralTemplates = listOf<SemanticsCoreTemplate>()
        private set

    fun generateNumeralParadigm(): NumeralParadigm {
        val base = NumeralSystemBase.values().randomElement()

        var threshold = -1
        when (base) {
            NumeralSystemBase.Decimal -> {
                generateTill20()
                numeralMeanings += listOf(100, 1000).map { it.toString() }

                ranges[100..100] = NumeralConstructionType.SingleWord
                ranges[1000..1000] = NumeralConstructionType.SingleWord
                ranges[21..99] = NumeralConstructionType.AddWord(
                    RelationArranger(StaticOrder(listOf(AdNumeral, SumNumeral, MulNumeral).shuffled())),
                    10
                )
                ranges[101..999] = NumeralConstructionType.AddWord(
                    RelationArranger(StaticOrder(listOf(AdNumeral, SumNumeral, MulNumeral).shuffled())),
                    100
                )
                ranges[1001..9999] = NumeralConstructionType.AddWord(
                    RelationArranger(StaticOrder(listOf(AdNumeral, SumNumeral, MulNumeral).shuffled())),
                    1000
                )

                threshold = 10000
            }
            NumeralSystemBase.Restricted3 -> {
                numeralMeanings += (1..3).map { it.toString() }
                ranges[1..3] = NumeralConstructionType.SingleWord
                threshold = 4
            }
            NumeralSystemBase.Restricted5 -> {
                numeralMeanings += (1..5).map { it.toString() }
                ranges[1..5] = NumeralConstructionType.SingleWord
                threshold = 6
            }
            NumeralSystemBase.Restricted20 -> {
                generateTill20()

                threshold = 21
            }
        }
        numeralMeanings += manyMeaning
        ranges[threshold..Int.MAX_VALUE] = NumeralConstructionType.SpecialWord(manyMeaning)

        numeralTemplates = numeralMeanings.map { SemanticsCoreTemplate(it, SpeechPart.Numeral) }

        return NumeralParadigm(base, ranges.toList())
    }

    private fun generateTill20() {
        uniqueTensChance.chanceOf {
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
    }
}
