package shmp.generator

import shmp.language.syntax.SyntaxParadigm
import kotlin.random.Random

class SyntaxParadigmGenerator(val random: Random) {
    internal fun generateSyntaxParadigm(): SyntaxParadigm {
        return SyntaxParadigm(random.nextDouble())
    }
}