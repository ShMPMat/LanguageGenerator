package shmp.lang.language.syntax.features


data class CopulaPresence(val copulaType: ParametrizedSyntaxFeatures<CopulaType>)


enum class CopulaType(override val probability: Double): SyntaxFeature {
    Verb(1.0),
    Particle(0.25),
    None(0.1)
}