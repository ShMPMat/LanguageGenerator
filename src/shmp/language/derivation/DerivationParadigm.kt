package shmp.language.derivation

class DerivationParadigm(val derivations: List<Derivation>, val compounds: List<Compound>) {
    override fun toString() = """
        |Derivations:
        |
        |${derivations.joinToString("\n")}
        |
        |Compositions:
        |
        |${compounds.joinToString("\n")}
    """.trimMargin()
}