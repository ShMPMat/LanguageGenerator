package shmp.language.derivation

class DerivationParadigm(val derivations: List<Derivation>) {
    override fun toString() = "Derivations:" +
            derivations.joinToString("\n", "\n", "\n")
}