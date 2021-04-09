package shmp.lang.language.syntax.features


data class PossessionConstructionPresence(val possessionConstructionType: SsoSyntaxFeatures<PossessionConstructionType>) {
    override fun toString() = possessionConstructionType.joinToString(", ")
}


enum class PossessionConstructionType(override val probability: Double): SyntaxFeature {
    HaveVerb(1.0)
}
