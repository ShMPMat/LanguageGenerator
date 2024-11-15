package io.tashtabash.lang.language.lexis

import io.tashtabash.lang.language.category.value.CategoryValue
import io.tashtabash.lang.language.LanguageException
import io.tashtabash.lang.language.derivation.ChangeHistory
import io.tashtabash.lang.language.derivation.Derivation
import io.tashtabash.lang.language.derivation.DerivationType
import io.tashtabash.random.UnwrappableSSO


data class SemanticsCore(
    val meaningCluster: MeaningCluster,
    val speechPart: TypedSpeechPart,
    val commonness: Double,
    val connotations: Connotations = Connotations(setOf()),
    val tags: Set<SemanticsTag> = setOf(),
    val derivationCluster: DerivationCluster = DerivationCluster(mapOf()),
    val staticCategories: Set<CategoryValue> = setOf(),
    val appliedDerivations: List<Derivation> = listOf(),
    val changeHistory: ChangeHistory? = null
) {
    constructor(meaning: Meaning, speechPart: TypedSpeechPart): this(meaning.toCluster(), speechPart, 1.0)

    init {
        if (speechPart.type == SpeechPart.Verb && (tags.none { it.name.contains("trans") } || tags.isEmpty()))
            throw LanguageException("Verb $meaningCluster doesn't have transitivity")

        derivationCluster.typeToCore.entries
            .firstOrNull { it.key.fromSpeechPart != speechPart.type }
            ?.let {
                throw LanguageException("$speechPart has a derivation for ${it.key.fromSpeechPart} ${it.key.name}")
            }
    }

    fun computeChangeDepth(lexis: AbstractLexis): Int =
        changeHistory?.computeChangeDepth(lexis)
            ?: 0

    fun hasMeaning(meaning: String) = meaning in meaningCluster.meanings

    override fun toString() = meaningCluster.toString()
}


data class SemanticsTag(val name: String)


data class DerivationCluster(val typeToCore: Map<DerivationType, List<DerivationLink>>)


data class DerivationLink(
    val template: Meaning?,
    override val probability: Double
) : UnwrappableSSO<Meaning?>(template)

val noDerivationLink = listOf(DerivationLink(null, 1.0))


data class CompoundLink(
    val templates: List<String>?,
    override val probability: Double
) : UnwrappableSSO<List<String>?>(templates)

val noCompoundLink = CompoundLink(null, 0.0)//TODO back to 1.0


typealias Meaning = String
