package shmp.lang.language.lexis

import shmp.lang.containers.SemanticsCoreTemplate
import shmp.lang.generator.util.GeneratorException
import shmp.lang.language.CategoryValue
import shmp.lang.language.LanguageException
import shmp.lang.language.derivation.Derivation
import shmp.lang.language.derivation.ChangeHistory
import shmp.lang.language.derivation.DerivationType
import shmp.random.UnwrappableSSO


data class SemanticsCore(
    val meaningCluster: MeaningCluster,
    val speechPart: TypedSpeechPart,
    val connotations: Connotations = Connotations(setOf()),
    val tags: Set<SemanticsTag> = setOf(),
    val derivationCluster: DerivationCluster = DerivationCluster(mapOf()),
    val staticCategories: Set<CategoryValue> = setOf(),
    val appliedDerivations: List<Derivation> = listOf(),
    val changeHistory: ChangeHistory? = null
) {
    init {
        if (speechPart.type == SpeechPart.Verb && (tags.none { it.name.contains("trans") } || tags.isEmpty()))
            throw LanguageException("Verb $meaningCluster doesn't have transitivity")

        derivationCluster.typeToCore.entries
            .firstOrNull { it.key.fromSpeechPart != speechPart.type }
            ?.let {
                throw LanguageException("$speechPart has a derivation for ${it.key.fromSpeechPart} ${it.key.name}")
            }
    }

    val changeDepth = changeHistory?.changeDepth ?: 0

    fun hasMeaning(meaning: String) = meaning in meaningCluster.meanings

    override fun toString() = meaningCluster.toString()
}


data class SemanticsTag(val name: String)


data class DerivationCluster(val typeToCore: Map<DerivationType, List<DerivationLink>>) {
    init {
        for ((type, lst) in typeToCore) {
            val existingTemplates = lst.mapNotNull { it.template }

            existingTemplates.firstOrNull { it.speechPart != type.toSpeechPart }?.let {
                throw GeneratorException(
                    "Derivation type ${type.toSpeechPart} doesn't equals word type ${it.speechPart} ${it.word}"
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

val noCompoundLink = CompoundLink(null, 0.0)//TODO back to 1.0


typealias Meaning = String
