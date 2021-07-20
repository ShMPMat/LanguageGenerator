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
import shmp.random.GenericSSO
import shmp.random.singleton.*
import shmp.random.toSampleSpaceObject
import kotlin.math.pow


class NumeralParadigmGenerator {
    private val manyMeaning: Meaning = "Many"
    private val uniqueTensChance = 0.5

    private val numeralMeanings = mutableListOf<String>()
    private val ranges = mutableMapOf<IntRange, NumeralConstructionType>()
    private var threshold = -1
    var numeralTemplates = listOf<SemanticsCoreTemplate>()
        private set

    fun generateNumeralParadigm(): NumeralParadigm {
        val base = NumeralSystemBase.values().randomElement()
        when (base) {
            NumeralSystemBase.Decimal -> {
//                generateTill20()
//                numeralMeanings += listOf("100", "1000")
//
//                ranges[100..100] = NumeralConstructionType.SingleWord
//                ranges[1000..1000] = NumeralConstructionType.SingleWord
//                ranges[21..99] = NumeralConstructionType.AddWord(
//                    RelationArranger(StaticOrder(listOf(AdNumeral, SumNumeral, MulNumeral).shuffled())),
//                    10
//                )
//                ranges[101..999] = NumeralConstructionType.AddWord(
//                    RelationArranger(StaticOrder(listOf(AdNumeral, SumNumeral, MulNumeral).shuffled())),
//                    100
//                )
//                ranges[1001..9999] = NumeralConstructionType.AddWord(
//                    RelationArranger(StaticOrder(listOf(AdNumeral, SumNumeral, MulNumeral).shuffled())),
//                    1000
//                )
//
//                threshold = 10000
                generateBasedSystem(
                    10,
                    1.toSampleSpaceObject(1.0),
                    2.toSampleSpaceObject(1.0),
                    3.toSampleSpaceObject(1.0),
                    4.toSampleSpaceObject(0.5)
                )
            }
            NumeralSystemBase.Restricted3 -> generateRestricted(3)
            NumeralSystemBase.Restricted5 -> generateRestricted(5)
            NumeralSystemBase.Restricted10 -> generateRestricted(10)
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

    private fun generateBasedSystem(base: Int, vararg powers: GenericSSO<Int>) {
        generateTill20()

        val actualPowers = powers.takeWhile { it.probability.testProbability() }
            .map { it.value }

        for ((i, powerSSO) in actualPowers.withIndex()) {
            val n = base.toDouble().pow(powerSSO).toInt()

            val nextN = if (i != actualPowers.size - 1)
                base.toDouble().pow(actualPowers[i + 1]).toInt()
            else n * n

            if (n <= 20) {
                ranges[21 until nextN] = NumeralConstructionType.AddWord(
                    RelationArranger(StaticOrder(listOf(AdNumeral, SumNumeral, MulNumeral).shuffled())),
                    n
                )
                continue
            }

            numeralMeanings += n.toString()
            ranges[n..n] = NumeralConstructionType.SingleWord
            val arranger = RelationArranger(StaticOrder(listOf(AdNumeral, SumNumeral, MulNumeral).shuffled()))
            ranges[n + 1 until nextN] = NumeralConstructionType.AddWord(arranger, n)
            threshold = nextN
        }
    }

    private fun generateRestricted(max: Int) {
        numeralMeanings += (1..max).map { it.toString() }
        ranges[1..max] = NumeralConstructionType.SingleWord
        threshold = max + 1
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
