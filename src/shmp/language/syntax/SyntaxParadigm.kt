package shmp.language.syntax

import shmp.random.SampleSpaceObject


class SyntaxParadigm(val copulaPresence: CopulaPresence)


data class CopulaPresence(val copulaType: CopulaType, val probability: Double)


enum class CopulaType(override val probability: Double): SampleSpaceObject {
    Verb(1.0)
}
