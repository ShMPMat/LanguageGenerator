package shmp.containers

import shmp.generator.GeneratorException
import shmp.language.SpeechPart
import shmp.language.category.genderName
import shmp.random.SampleSpaceObject
import java.io.File

class WordBase() {
    val words: MutableList<SyntaxCoreTemplate> = ArrayList()

    init {
        File("SupplementFiles/Words").forEachLine { line ->
            if (!line.isBlank() && line[0] != '/') {
                val tokens = line.split(" +".toRegex())
                val word = tokens[0]
                val speechPart = SpeechPart.valueOf(tokens[1])
                words.add(SyntaxCoreTemplate(
                    word,
                    speechPart,
                    tokens.drop(2)
                        .map { SyntaxTagCluster(
                            parseSyntaxTagTemplates(it.drop(2)),
                            getType(it.take(1))
                        ) }
                        .toSet()
                ))
            }
        }
    }
}

fun parseSyntaxTagTemplates(string: String) = string
    .split(",")
    .map { SyntaxTagTemplate(it.split(":")[0], it.split(":")[1].toDouble()) }

fun getType(string: String) = when(string) {
    "G" -> genderName
    else -> throw GeneratorException("Unknown syntax tag type alias $string")
}

data class SyntaxCoreTemplate(val word: String, val speechPart: SpeechPart, val tagClusters: Set<SyntaxTagCluster>)

data class SyntaxTagCluster(val syntaxTags: List<SyntaxTagTemplate>, val type: String)

data class SyntaxTagTemplate(val name: String, override val probability: Double): SampleSpaceObject