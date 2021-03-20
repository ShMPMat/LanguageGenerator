package shmp.lang.containers

import shmp.lang.generator.util.GeneratorException
import shmp.lang.language.CategoryValue
import shmp.lang.language.SpeechPart
import shmp.lang.language.derivation.DerivationType
import shmp.lang.language.lexis.*
import shmp.random.SampleSpaceObject
import shmp.random.UnwrappableSSO
import shmp.random.randomUnwrappedElement
import kotlin.random.Random


data class SemanticsCoreTemplate(
    val word: Meaning,
    val speechPart: SpeechPart,
    val tagClusters: Set<SemanticsTagCluster>,
    val derivationClusterTemplate: DerivationClusterTemplate,
    override val probability: Double
) : SampleSpaceObject

fun SemanticsCoreTemplate.toSemanticsCore(staticCategories: Set<CategoryValue>, random: Random) =
    SemanticsCore(
        MeaningCluster(word),
        this.speechPart,
        this.tagClusters
            .filter { it.shouldBeInstantiated }
            .map {
                SemanticsTag(randomUnwrappedElement(it.semanticsTags, random))
            }
            .toSet(),
        DerivationCluster(this.derivationClusterTemplate.typeToCore),
        staticCategories
    )

fun SemanticsCoreTemplate.merge(core: SemanticsCore, random: Random): SemanticsCore {
    if (this.speechPart != core.speechPart)
        throw GeneratorException("Core merge error: $core and ${this.word} has different speech parts")

    return SemanticsCore(
        MeaningCluster(core.meaningCluster.meanings + listOf(this.word)),
        core.speechPart,
        this.tagClusters
            .filter { it.type.isNotBlank() && it.type[0].isLowerCase() }
            .map {
                SemanticsTag(randomUnwrappedElement(it.semanticsTags, random))
            }
            .toSet() + core.tags,
        core.derivationCluster.merge(this.derivationClusterTemplate.typeToCore),
        core.staticCategories
    )
}

fun DerivationCluster.merge(newEntries: Map<DerivationType, List<DerivationLink>>): DerivationCluster {
    val newMap = this.typeToCore.toMutableMap()
    newEntries.entries.forEach { (t, ls) ->
        val old = newMap[t] ?: listOf()
        newMap[t] = old + ls
    }
    return DerivationCluster(newMap)
}

data class DerivationClusterTemplate(
    val typeToCore: MutableMap<DerivationType, List<DerivationLink>> = mutableMapOf(),
    val possibleCompounds: MutableList<CompoundLink> = mutableListOf(),
    val appliedDerivations: Set<DerivationType> = setOf()
)

data class SemanticsTagCluster(
    val semanticsTags: List<SemanticsTagTemplate>,
    val type: String,
    val shouldBeInstantiated: Boolean
)

data class SemanticsTagTemplate(val name: String, override val probability: Double) : UnwrappableSSO<String>(name)
