package shmp.containers

import shmp.generator.GeneratorException
import shmp.language.SpeechPart
import shmp.language.category.animosityName
import shmp.language.category.genderName
import shmp.random.SampleSpaceObject
import java.io.File

class WordBase() {
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
                        .toSet()
                ))
            }
        }
    }
}

fun parseSemanticsTagTemplates(string: String) = string
    .split(",")
    .map { SemanticsTagTemplate(it.split(":")[0], it.split(":")[1].toDouble()) }

fun getType(string: String) = when(string) {
    "G" -> genderName
    "A" -> animosityName
    "T" -> "transitivity"
    else -> throw GeneratorException("Unknown SemanticsTag type alias $string")
}

data class SemanticsCoreTemplate(val word: String, val speechPart: SpeechPart, val tagClusters: Set<SemanticsTagCluster>)

data class SemanticsTagCluster(val semanticsTags: List<SemanticsTagTemplate>, val type: String)

data class SemanticsTagTemplate(val name: String, override val probability: Double): SampleSpaceObject