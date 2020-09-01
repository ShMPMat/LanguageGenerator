package shmp.language.derivation

class DerivationParadigm(val derivations: List<Derivation>, val compounds: List<Compound>) {
    override fun toString() = """
        |Derivations:
        |
        |${derivations.joinToString("\n")}
        |
        |Compounds:
        |
        |${compounds.joinToString("\n")}
    """.trimMargin()
}