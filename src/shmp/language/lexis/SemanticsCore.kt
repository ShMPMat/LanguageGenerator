package shmp.language.lexis

import shmp.containers.SemanticsCoreTemplate
import shmp.generator.GeneratorException
import shmp.language.CategoryValue
import shmp.language.LanguageException
import shmp.language.SpeechPart
import shmp.language.derivation.Derivation
import shmp.language.derivation.DerivationType
import shmp.random.UnwrappableSSO


data class SemanticsCore(
    val words: List<Meaning>,
    val speechPart: SpeechPart,
    val tags: Set<SemanticsTag>,
    val derivationCluster: DerivationCluster = DerivationCluster(mapOf()),
    val staticCategories: Set<CategoryValue> = setOf(),
    val appliedDerivations: List<Derivation> = listOf(),
    val derivationHistory: DerivationHistory? = null
) {
    init {
        if (speechPart == SpeechPart.Verb && (tags.none { it.name.contains("trans") } || tags.isEmpty()))
            throw LanguageException("Verb $this doesn't have transitivity")

        derivationCluster.typeToCore.keys
            .firstOrNull { it.fromSpeechPart != speechPart }
            ?.let {
                throw LanguageException("$speechPart has a derivation for ${it.fromSpeechPart}")
            }
    }

    val meanings = words

    fun hasMeaning(meaning: String) = meaning in meanings

    override fun toString() = words.joinToString()
}


data class SemanticsTag(val name: String)


data class DerivationCluster(val typeToCore: Map<DerivationType, List<DerivationLink>>) {
    init {
        for ((type, lst) in typeToCore) {
            val existingTemplates = lst.mapNotNull { it.template }

            existingTemplates.firstOrNull { it.speechPart != type.toSpeechPart }?.let {
                throw GeneratorException(
                    "Derivation type ${type.toSpeechPart} doesn't equals word type ${it.speechPart}"
                )
            }
        }
    }
}


data class DerivationLink(
    val template: SemanticsCoreTemplate?,
    override val probability: Double
) : UnwrappableSSO<SemanticsCoreTemplate?>(template)

val noDerivationLink = listOf(DerivationLink(null, 1.0))


data class CompoundLink(
    val templates: List<SemanticsCoreTemplate>?,
    override val probability: Double
) : UnwrappableSSO<List<SemanticsCoreTemplate>?>(templates)

val noCompoundLink = listOf(CompoundLink(null, 0.0)) //TODO back to 1.0


data class DerivationHistory(val derivation: Derivation, val parent: Word)


typealias Meaning = String
