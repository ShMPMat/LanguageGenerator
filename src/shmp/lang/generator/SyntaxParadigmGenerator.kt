package shmp.lang.generator

import shmp.lang.language.syntax.SyntaxParadigm
import shmp.lang.language.syntax.features.*
import shmp.random.singleton.RandomSingleton
import shmp.random.singleton.chanceOf
import shmp.random.singleton.randomElement
import shmp.random.singleton.testProbability


class SyntaxParadigmGenerator {
    internal fun generateSyntaxParadigm(): SyntaxParadigm {
        val mainCopulaType = CopulaType.values().randomElement()
        val noneProbability = RandomSingleton.random.nextDouble(mainCopulaType.probability)
            .let {
                if (it <= mainCopulaType.probability / 5) 0.0
                else it
            }
        val copulaPresence = CopulaPresence(
            listOf(mainCopulaType.parametrize(mainCopulaType.probability)) +
                    if (noneProbability != 0.0)
                        listOf(CopulaType.None.parametrize(noneProbability))
                    else listOf()
        )

        val questionMarkerPresence = QuestionMarkerPresence(QuestionMarker.takeIf { 0.6.testProbability() })

        return SyntaxParadigm(
            copulaPresence,
            questionMarkerPresence
        )
    }
}
