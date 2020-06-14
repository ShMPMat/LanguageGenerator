package shmp.containers

import shmp.generator.GeneratorException
import shmp.language.CategoryValue
import shmp.language.SpeechPart
import shmp.language.category.animosityName
import shmp.language.category.genderName
import shmp.language.derivation.DerivationType
import shmp.language.lexis.DerivationCluster
import shmp.language.lexis.DerivationLink
import shmp.language.lexis.SemanticsCore
import shmp.language.lexis.SemanticsTag
import shmp.random.SampleSpaceObject
import shmp.random.randomElement
import java.io.File
import kotlin.random.Random

class WordBase {
    val words: MutableList<SemanticsCoreTemplate> = ArrayList()

    init {
        File("SupplementFiles/Words").forEachLine { line ->
            if (!line.isBlank() && line[0] != '/') {
                val tokens = line.split(" +".toRegex())
                val word = tokens[0]
                val speechPart = SpeechPart.valueOf(tokens[1])
                words.add(SemanticsCoreTemplate(
                    word,
                    speechPart,
                    tokens.drop(2)
                        .map { SemanticsTagCluster(
                            parseSemanticsTagTemplates(it.drop(2)),
                            getType(it.take(1))
                        ) }
                        .toSet(),
                    DerivationClusterTemplate()
                ))
            }
        }
    }
}

fun parseSemanticsTagTemplates(string: String) = string
    .split(",")
    .map {
        val (name, prob) = it.split(":")
        SemanticsTagTemplate(name, prob.toDouble())
    }

fun getType(string: String) = when(string) {
    "G" -> genderName
    "A" -> animosityName
    "T" -> "transitivity"
    else -> throw GeneratorException("Unknown SemanticsTag type alias $string")
}

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
        .toSet(),//TODO DERIVATION!!!!
    DerivationCluster(this.derivationClusterTemplate.typeToCore),
    staticCategories
)

data class DerivationClusterTemplate(val typeToCore: MutableMap<DerivationType, DerivationLink> = mutableMapOf())

data class SemanticsTagCluster(val semanticsTags: List<SemanticsTagTemplate>, val type: String)

data class SemanticsTagTemplate(val name: String, override val probability: Double): SampleSpaceObject