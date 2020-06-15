package shmp.containers

import shmp.generator.GeneratorException
import shmp.language.SpeechPart
import shmp.language.category.animosityName
import shmp.language.category.genderName
import java.io.File

class WordBase(supplementPath: String) {
    val baseWords: MutableList<SemanticsCoreTemplate> = ArrayList()
    val allWords: MutableList<SemanticsCoreTemplate> = ArrayList()

    init {
        File("$supplementPath/Words").forEachLine { line ->
            if (!line.isBlank() && line[0] != '/') {
                val tokens = line.split(" +".toRegex())
                val word = tokens[0]
                val speechPart = SpeechPart.valueOf(tokens[1])
                baseWords.add(SemanticsCoreTemplate(
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
        allWords.addAll(baseWords)
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
