package shmp.lang.language.syntax.features


data class PredicatePossessionPresence(val predicatePossessionType: SsoSyntaxFeatures<PredicatePossessionType>) {
    override fun toString() = predicatePossessionType.joinToString(", ")
}


enum class PredicatePossessionType(override val probability: Double): SyntaxFeature {
    HaveVerb(1.0)
}
