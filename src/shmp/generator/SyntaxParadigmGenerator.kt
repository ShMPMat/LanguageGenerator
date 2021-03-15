package shmp.generator

import shmp.language.syntax.SyntaxParadigm
import shmp.language.syntax.features.CopulaPresence
import shmp.language.syntax.features.CopulaType
import shmp.language.syntax.features.parametrize
import shmp.random.randomElement
import shmp.random.singleton.RandomSingleton
import shmp.random.singleton.randomElement
import kotlin.random.Random


class SyntaxParadigmGenerator() {
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

        return SyntaxParadigm(
            copulaPresence
        )
    }
}
