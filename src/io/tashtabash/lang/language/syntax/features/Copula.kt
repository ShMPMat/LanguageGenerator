package io.tashtabash.lang.language.syntax.features


data class CopulaPresence(val copulaType: SsoSyntaxFeatures<CopulaType>) {
    override fun toString() = copulaType.joinToString(", ")
}


enum class CopulaType(override val probability: Double): SyntaxFeature {
    Verb(1.0),
    Particle(0.25),
    None(0.1)
}
