package shmp.containers

import shmp.language.CategoryValue
import shmp.language.SpeechPart
import shmp.language.derivation.DerivationType
import shmp.language.lexis.DerivationCluster
import shmp.language.lexis.DerivationLink
import shmp.language.lexis.SemanticsCore
import shmp.language.lexis.SemanticsTag
import shmp.random.SampleSpaceObject
import shmp.random.randomElement
import kotlin.random.Random


data class SemanticsCoreTemplate(
    val word: String,
    val speechPart: SpeechPart,
    val tagClusters: Set<SemanticsTagCluster>,
    val derivationClusterTemplate: DerivationClusterTemplate
)

fun SemanticsCoreTemplate.toSemanticsCore(staticCategories: Set<CategoryValue>, random: Random) = SemanticsCore(
    this.word,
    this.speechPart,
    this.tagClusters
        .filter { it.type.isNotBlank() && it.type[0].isLowerCase() }
        .map {
            SemanticsTag(
                randomElement(
                    it.semanticsTags,
                    random
                ).name
            )
        }
        .toSet(),
    DerivationCluster(this.derivationClusterTemplate.typeToCore),
    staticCategories
)

data class DerivationClusterTemplate(
    val typeToCore: MutableMap<DerivationType, List<DerivationLink>> = mutableMapOf(),
    val appliedDerivations: Set<DerivationType> = setOf()
)

data class SemanticsTagCluster(val semanticsTags: List<SemanticsTagTemplate>, val type: String)

data class SemanticsTagTemplate(val name: String, override val probability: Double): SampleSpaceObject