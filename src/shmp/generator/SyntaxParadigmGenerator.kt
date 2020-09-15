package shmp.generator

import shmp.language.syntax.CopulaPresence
import shmp.language.syntax.CopulaType
import shmp.language.syntax.SyntaxParadigm
import shmp.random.randomElement
import kotlin.random.Random


class SyntaxParadigmGenerator(val random: Random) {
    internal fun generateSyntaxParadigm(): SyntaxParadigm {
        val copulaPresence = CopulaPresence(
            randomElement(CopulaType.values(), random),
            random.nextDouble()
        )

        return SyntaxParadigm(
            copulaPresence
        )
    }
}
