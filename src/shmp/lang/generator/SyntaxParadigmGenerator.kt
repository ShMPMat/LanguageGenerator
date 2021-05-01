package shmp.lang.generator

import shmp.lang.language.category.CaseValue
import shmp.lang.language.category.caseName
import shmp.lang.language.category.paradigm.WordChangeParadigm
import shmp.lang.language.syntax.SyntaxParadigm
import shmp.lang.language.syntax.features.*
import shmp.random.singleton.RandomSingleton
import shmp.random.singleton.randomElement
import shmp.random.singleton.testProbability


class SyntaxParadigmGenerator {
    internal fun generateSyntaxParadigm(wordChangeParadigm: WordChangeParadigm): SyntaxParadigm {
        val mainCopulaType = CopulaType.values().randomElement()
        val noneProbability = RandomSingleton.random.nextDouble(mainCopulaType.probability)
            .let {
                if (it <= mainCopulaType.probability / 5) 0.0
                else it
            }
        val copulaPresence = CopulaPresence(
            listOf(mainCopulaType.toSso(mainCopulaType.probability)) +
                    if (noneProbability != 0.0)
                        listOf(CopulaType.None.toSso(noneProbability))
                    else listOf()
        )

        val questionMarkerPresence = QuestionMarkerPresence(QuestionMarker.takeIf { 0.6.testProbability() })

        val possiblePossessionType = PredicatePossessionType.values().toMutableList()

        if (!wordChangeParadigm.categories.first { it.outType == caseName }.actualValues.contains(CaseValue.Topic)) {
            possiblePossessionType.remove(PredicatePossessionType.Topic)
        }

        val possessionConstructionPresence = PredicatePossessionPresence(
            listOf(possiblePossessionType.randomElement().toSso(1.0))
        )

        return SyntaxParadigm(
            copulaPresence,
            questionMarkerPresence,
            possessionConstructionPresence
        )
    }
}
