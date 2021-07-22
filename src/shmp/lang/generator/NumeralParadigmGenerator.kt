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

    private val numeralMeanings = mutableSetOf<String>()
    private val ranges = mutableMapOf<IntRange, NumeralConstructionType>()
    private var threshold = -1
    var numeralTemplates = listOf<SemanticsCoreTemplate>()
        private set

    fun generateNumeralParadigm(): NumeralParadigm {
        val base = NumeralSystemBase.values().randomElement()
        when (base) {
            NumeralSystemBase.Decimal -> generateBasedSystem(10, (1..4).zipSSO(1.0, 1.0, 1.0, 0.5))
            NumeralSystemBase.Vigesimal -> generateBasedSystem(20, (1..4).zipSSO(1.0, 1.0, 0.75, 0.25))
            NumeralSystemBase.VigesimalTill100 -> {
                generateBasedSystem(20, (1..1).zipSSO(1.0), cap = 100)
                generateBasedSystem(10, (2..4).zipSSO(1.0, 1.0, 0.5), hasPrefix = true)
            }
            NumeralSystemBase.SixtyBased -> generateBasedSystem(60, (1..4).zipSSO(1.0, 0.9, 0.7, 0.15))
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

        numeralTemplates = numeralMeanings.sorted().map { SemanticsCoreTemplate(it, SpeechPart.Numeral) }

        return NumeralParadigm(base, ranges.toList())
    }

    private fun generateBasedSystem(
        base: Int,
        powers: List<GenericSSO<Int>>,
        cap: Int? = null,
        hasPrefix: Boolean = false
    ) {
        val actualNumbers = powers.takeWhile { it.probability.testProbability() }
            .map { base.toDouble().pow(it.value).toInt() }
        val actualCap = cap ?: actualNumbers.last() * actualNumbers.last()

        if (!hasPrefix) {
            generateTill20()

            val (firstBorder, firstBase) = if (actualNumbers[0] <= 20)
                (if (actualNumbers.size > 1) actualNumbers[1] else actualCap) to actualNumbers[0]
            else
                actualNumbers[0] to 20
            ranges[21 until firstBorder] = NumeralConstructionType.AddWord(
                RelationArranger(StaticOrder(listOf(AdNumeral, SumNumeral, MulNumeral).shuffled())),
                firstBase
            )
        }

        for ((i, n) in actualNumbers.withIndex()) {
            if (n <= 20)
                continue

            val nextN = if (i != actualNumbers.size - 1)
                actualNumbers[i + 1]
            else actualCap

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

    private fun IntRange.zipSSO(vararg ps: Double) = zip(ps.toList())
        .map { (n, p) -> n.toSampleSpaceObject(p) }
}
