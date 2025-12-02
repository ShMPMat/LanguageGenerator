package io.tashtabash.lang.containers

import io.tashtabash.lang.generator.util.GeneratorException
import io.tashtabash.lang.language.category.value.CategoryValue
import io.tashtabash.lang.language.lexis.SpeechPart
import io.tashtabash.lang.language.derivation.DerivationType
import io.tashtabash.lang.language.lexis.*
import io.tashtabash.random.SampleSpaceObject
import io.tashtabash.random.UnwrappableSSO
import io.tashtabash.random.randomUnwrappedElement
import io.tashtabash.random.singleton.randomUnwrappedElement
import kotlin.random.Random


data class SemanticsCoreTemplate(
    val word: Meaning,
    val speechPart: SpeechPart,
    val connotations: Connotations = Connotations(),
    val tagClusters: MutableSet<SemanticsTagCluster> = mutableSetOf(),
    val derivationClusterTemplate: DerivationClusterTemplate = DerivationClusterTemplate(),
    override val probability: Double = 1.0
) : SampleSpaceObject

fun SemanticsCoreTemplate.toSemanticsCore(staticCategories: Set<CategoryValue>): SemanticsCore {
    val tags = tagClusters.filter { it.shouldInstantiate }
        .map { SemanticsTag(it.tags.randomUnwrappedElement()) }
        .toSet()

    return SemanticsCore(
        MeaningCluster(word),
        typeSpeechPart(tags),
        probability,
        connotations,
        tags,
        DerivationCluster(derivationClusterTemplate.typeToCore),
        staticCategories
    )
}

private fun SemanticsCoreTemplate.typeSpeechPart(tags: Set<SemanticsTag>): TypedSpeechPart {
    if (speechPart == SpeechPart.Verb)
        return if (tags.any { it.name == "intrans" })
            speechPart.toIntransitive()
        else if (tags.any { it.name == "trans" })
            speechPart.toDefault()
        else
            throw GeneratorException("Verb template has no transitivity tag, can't instantiate")

    return speechPart.toDefault()
}

fun SemanticsCoreTemplate.merge(core: SemanticsCore, random: Random): SemanticsCore {
    if (speechPart != core.speechPart.type)
        throw GeneratorException("Core merge error: $core and $word has different speech parts")

    return SemanticsCore(
        MeaningCluster(core.meaningCluster.meanings + word),
        core.speechPart,
        (probability + core.commonness) / 2,
        connotations + core.connotations,
        tagClusters.filter { it.type.isNotBlank() && it.type[0].isLowerCase() }
            .map {
                SemanticsTag(randomUnwrappedElement(it.tags, random))
            }
            .toSet() + core.tags,
        core.derivationCluster.merge(derivationClusterTemplate.typeToCore),
        core.staticCategories
    )
}

fun DerivationCluster.merge(newEntries: Map<DerivationType, List<DerivationLink>>): DerivationCluster {
    val newMap = typeToCore.toMutableMap()
    newEntries.forEach { (t, ls) ->
        val old = newMap.getOrDefault(t, listOf())
        newMap[t] = old + ls
    }
    return DerivationCluster(newMap)
}

data class DerivationClusterTemplate(
    val typeToCore: MutableMap<DerivationType, MutableList<DerivationLink>> = mutableMapOf(),
    val possibleCompounds: MutableList<CompoundLink> = mutableListOf(),
    val appliedDerivations: Set<DerivationType> = setOf()
)

data class SemanticsTagCluster(val tags: List<SemanticsTagTemplate>, val type: String, val shouldInstantiate: Boolean) {
    fun hasTag(tagName: String): Boolean =
        tags.any { it.name == tagName }
}

data class SemanticsTagTemplate(val name: String, override val probability: Double = 1.0) : UnwrappableSSO<String>(name)
