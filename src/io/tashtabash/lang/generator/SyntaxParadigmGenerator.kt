package io.tashtabash.lang.generator

import io.tashtabash.lang.language.category.CaseValue
import io.tashtabash.lang.language.category.caseName
import io.tashtabash.lang.language.category.paradigm.WordChangeParadigm
import io.tashtabash.lang.language.syntax.SyntaxParadigm
import io.tashtabash.lang.language.syntax.features.*
import io.tashtabash.random.singleton.RandomSingleton
import io.tashtabash.random.singleton.randomElement
import io.tashtabash.random.singleton.testProbability


class SyntaxParadigmGenerator {
    internal fun generateSyntaxParadigm(wordChangeParadigm: WordChangeParadigm): SyntaxParadigm {
        val copulaPresence = generateCopula()
        val questionMarkerPresence = QuestionMarkerPresence(QuestionMarker.takeIf { 0.6.testProbability() })

        val possiblePossessionType = PredicatePossessionType.entries.toMutableList()
        if (!wordChangeParadigm.categories.first { it.outType == caseName }.actualValues.contains(CaseValue.Topic))
            possiblePossessionType.remove(PredicatePossessionType.Topic)

        val possessionConstructionPresence = PredicatePossessionPresence(
            listOf(possiblePossessionType.randomElement().toSso(1.0))
        )

        return SyntaxParadigm(
            copulaPresence,
            questionMarkerPresence,
            possessionConstructionPresence
        )
    }

    private fun generateCopula(): CopulaPresence {
        val mainCopulaType = CopulaType.entries.randomElement()
        val noneProbability = RandomSingleton.random.nextDouble(mainCopulaType.probability)
            .let {
                if (it <= mainCopulaType.probability / 5) 0.0
                else it
            }
        return CopulaPresence(
            listOf(mainCopulaType.toSso(mainCopulaType.probability)) +
                    if (noneProbability != 0.0)
                        listOf(CopulaType.None.toSso(noneProbability))
                    else listOf()
        )
    }
}
