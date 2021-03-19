package shmp.language.syntax.features


data class CopulaPresence(val copulaType: ParametrizedSyntaxFeatures<CopulaType>)


enum class CopulaType(override val probability: Double): SyntaxFeature {
    Verb(1.0),
    Particle(0.25),
//    Particle(0.0),
//    None(0.1)
    None(0.0)
}
