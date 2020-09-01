package shmp.containers

import shmp.generator.GeneratorException
import shmp.language.CategoryValue
import shmp.language.SpeechPart
import shmp.language.derivation.DerivationType
import shmp.language.lexis.*
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

fun SemanticsCoreTemplate.toSemanticsCore(staticCategories: Set<CategoryValue>, random: Random) = SemanticsCore(
    listOf(this.word),
    this.speechPart,
    this.tagClusters
        .filter { it.type.isNotBlank() && it.type[0].isLowerCase() }
        .map {
            SemanticsTag(
                randomUnwrappedElement(
                    it.semanticsTags,
                    random
                )
            )
        }
        .toSet(),
    DerivationCluster(this.derivationClusterTemplate.typeToCore),
    staticCategories
)

fun SemanticsCoreTemplate.merge(core: SemanticsCore, random: Random): SemanticsCore {
    if (this.speechPart != core.speechPart)
        throw GeneratorException("Core merge error: $core and ${this.word} has different speech parts")

    return SemanticsCore(
        core.words + listOf(this.word),
        core.speechPart,
        this.tagClusters
            .filter { it.type.isNotBlank() && it.type[0].isLowerCase() }
            .map {
                SemanticsTag(
                    randomUnwrappedElement(
                        it.semanticsTags,
                        random
                    )
                )
            }
            .toSet() + core.tags,
        core.derivationCluster.merge(this.derivationClusterTemplate.typeToCore),
        core.staticCategories
    )
}

fun DerivationCluster.merge(newEntries: Map<DerivationType, List<DerivationLink>>): DerivationCluster{
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

data class SemanticsTagCluster(val semanticsTags: List<SemanticsTagTemplate>, val type: String)

data class SemanticsTagTemplate(val name: String, override val probability: Double): UnwrappableSSO<String>(name)
