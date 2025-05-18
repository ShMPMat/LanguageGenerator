package io.tashtabash.lang.generator

import io.tashtabash.lang.containers.SemanticsCoreTemplate
import io.tashtabash.lang.language.NumeralSystemBase
import io.tashtabash.lang.language.lexis.Meaning
import io.tashtabash.lang.language.lexis.SpeechPart
import io.tashtabash.lang.language.syntax.numeral.NumeralConstructionType
import io.tashtabash.lang.language.syntax.numeral.NumeralParadigm
import io.tashtabash.lang.language.syntax.StaticOrder
import io.tashtabash.lang.language.syntax.SyntaxRelation.*
import io.tashtabash.lang.language.syntax.arranger.RelationArranger
import io.tashtabash.random.GenericSSO
import io.tashtabash.random.singleton.*
import io.tashtabash.random.toSampleSpaceObject
import kotlin.math.pow


class NumeralParadigmGenerator {
    private val manyMeaning: Meaning = "Many"
    private val uniqueTensChance = 0.5
    private val generic20Chance = 0.5

    private val numeralMeanings = mutableSetOf<String>()
    private val ranges = mutableMapOf<IntRange, NumeralConstructionType>()
    private var threshold = -1
    private var newArrangerProb = 0.1
    private var arranger = generateNewArranger()
    var numeralTemplates = listOf<SemanticsCoreTemplate>()
        private set

    fun generateNumeralParadigm(): NumeralParadigm {
        val base = NumeralSystemBase.entries.randomElement()
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
            NumeralSystemBase.Restricted20 -> generateTill20(false)
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
            generateTill20(true)

            val (border, firstBase) = if (actualNumbers[0] <= 20)
                (if (actualNumbers.size > 1) actualNumbers[1] else actualCap) to actualNumbers[0]
            else
                actualNumbers[0] to 20
            ranges[21 until border] = NumeralConstructionType.AddWord(generateArranger(), firstBase, generateOneProb())
        }

        for ((i, n) in actualNumbers.withIndex()) {
            if (n <= 20)
                continue

            val nextN = if (i != actualNumbers.size - 1)
                actualNumbers[i + 1]
            else actualCap

            (n..n).addUniqueRange()
            ranges[n + 1 until nextN] = NumeralConstructionType.AddWord(generateArranger(), n, generateOneProb())
            threshold = nextN
        }
    }

    private fun generateRestricted(max: Int) {
        numeralMeanings += (1..max).map { it.toString() }
        ranges[1..max] = NumeralConstructionType.SingleWord
        threshold = max + 1
    }

    private fun generateTill20(generic20Possibility: Boolean) {
        val isGeneric20 = generic20Possibility && generic20Chance.testProbability()
        uniqueTensChance.chanceOf {
            if (isGeneric20) {
                (1..19).addUniqueRange()
                ranges[20..20] = NumeralConstructionType.AddWord(generateArranger(), 10, generateOneProb())
            } else (1..20).addUniqueRange()
        } otherwise {
            (1..10).addUniqueRange()

            if (isGeneric20)
                ranges[11..20] = NumeralConstructionType.AddWord(generateArranger(), 10, generateOneProb())
            else {
                (20..20).addUniqueRange()
                ranges[11..19] = NumeralConstructionType.AddWord(generateArranger(), 10, generateOneProb())
            }
        }
        threshold = 21
    }

    private fun IntRange.addUniqueRange() {
        numeralMeanings += map { it.toString() }
        ranges[this] = NumeralConstructionType.SingleWord
    }

    private fun IntRange.zipSSO(vararg ps: Double) = zip(ps.toList())
        .map { (n, p) -> n.toSampleSpaceObject(p) }

    private fun generateArranger(): RelationArranger {
        newArrangerProb.chanceOf {
            arranger = generateNewArranger()
            newArrangerProb /= 2
        }

        return arranger
    }

    private fun generateNewArranger(): RelationArranger {
        val order = listOf(
            listOf(MulNumeral, AdNumeral, SumNumeral),
            listOf(AdNumeral, MulNumeral, SumNumeral),
            listOf(SumNumeral, AdNumeral, MulNumeral),
            listOf(SumNumeral, MulNumeral, AdNumeral),
        ).randomElement()

        return RelationArranger(StaticOrder(order))
    }

    private fun generateOneProb() = 0.1.chanceOf<Double> {
        RandomSingleton.random.nextDouble(0.7, 1.0)
    } ?: 0.0
}
