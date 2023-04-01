package io.tashtabash.lang.language.syntax.features


data class PredicatePossessionPresence(val predicatePossessionType: SsoSyntaxFeatures<PredicatePossessionType>) {
    override fun toString() = predicatePossessionType.joinToString(", ")
}


enum class PredicatePossessionType(override val probability: Double): SyntaxFeature {
    HaveVerb(63.0),
    LocativeOblique(24.0),
    DativeOblique(24.0),
    GenitiveOblique(22.0),
    Topic(100.0),
//    Conjunctional(59.0)
}
