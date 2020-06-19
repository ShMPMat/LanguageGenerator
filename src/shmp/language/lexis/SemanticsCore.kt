package shmp.language.lexis

import shmp.containers.SemanticsCoreTemplate
import shmp.language.CategoryValue
import shmp.language.SpeechPart
import shmp.language.derivation.Derivation
import shmp.language.derivation.DerivationType
import shmp.random.SampleSpaceObject

data class SemanticsCore(
    val word: String,
    val speechPart: SpeechPart,
    val tags: Set<SemanticsTag>,
    val derivationCluster: DerivationCluster = DerivationCluster(mapOf()),
    val staticCategories: Set<CategoryValue> = setOf(),
    val appliedDerivations: List<Derivation> = listOf()
)

data class SemanticsTag(val name: String)

data class DerivationCluster(val typeToCore: Map<DerivationType, List<DerivationLink>>)

data class DerivationLink(val template: SemanticsCoreTemplate?, override val probability: Double): SampleSpaceObject

val noDerivationLink = listOf(DerivationLink(null, 0.0)) //TODO back to 1.0
