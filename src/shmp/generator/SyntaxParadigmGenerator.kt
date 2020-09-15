package shmp.generator

import shmp.language.syntax.SyntaxParadigm
import shmp.language.syntax.features.CopulaPresence
import shmp.language.syntax.features.CopulaType
import shmp.language.syntax.features.parametrize
import shmp.random.randomElement
import kotlin.random.Random


class SyntaxParadigmGenerator(val random: Random) {
    internal fun generateSyntaxParadigm(): SyntaxParadigm {
        val copulaType = randomElement(CopulaType.values(), random)
        val noneProbability = random.nextDouble(copulaType.probability)
            .let {
                if (it <= copulaType.probability / 5) 0.0
                else it
            }
        val copulaPresence = CopulaPresence(
            listOf(
                copulaType.parametrize(copulaType.probability),
                CopulaType.None.parametrize(noneProbability)
            )
        )

        return SyntaxParadigm(
            copulaPresence
        )
    }
}
