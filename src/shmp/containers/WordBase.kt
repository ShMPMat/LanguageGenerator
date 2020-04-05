package shmp.containers

import shmp.language.SpeechPart
import shmp.language.SyntaxTag
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
                    tokens.drop(2).map { SyntaxTagCluster(parseSyntaxTagTemplates(it)) }.toSet()
                ))
            }
        }
    }
}

fun parseSyntaxTagTemplates(string: String) = string
    .split(",")
    .map { SyntaxTagTemplate(it.split(":")[0], it.split(":")[1].toDouble()) }

data class SyntaxCoreTemplate(val word: String, val speechPart: SpeechPart, val tagClusters: Set<SyntaxTagCluster>)

data class SyntaxTagCluster(val syntaxTags: List<SyntaxTagTemplate>)

data class SyntaxTagTemplate(val name: String, override val probability: Double): SampleSpaceObject