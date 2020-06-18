package shmp.containers

import shmp.generator.GeneratorException
import shmp.language.SpeechPart
import shmp.language.category.animosityName
import shmp.language.category.genderName
import shmp.language.derivation.DerivationType
import shmp.language.lexis.DerivationLink
import java.io.File

class WordBase(supplementPath: String) {
    val baseWords: MutableList<SemanticsCoreTemplate> = ArrayList()
    val allWords: MutableList<SemanticsCoreTemplate> = ArrayList()

    init {
        val wordsAndDataMap = mutableMapOf<String, Pair<SemanticsCoreTemplate, List<String>>>()

        File("$supplementPath/Words").forEachLine { line ->
            if (!line.isBlank() && line[0] != '/') {
                val tokens = line.split(" +".toRegex())
                val word = tokens[0]
                val speechPart = SpeechPart.valueOf(tokens[1])
                val tags = tokens.filter { it.contains("|") }
                val derivations = tokens.filter { it.contains("@") }

                val core = SemanticsCoreTemplate(
                    word,
                    speechPart,
                    tags.map {
                        val (name, tags) = it.split("|")
                        SemanticsTagCluster(
                            parseSemanticsTagTemplates(tags),
                            getType(name)
                        )
                    }.toSet(),
                    DerivationClusterTemplate()
                )
                wordsAndDataMap[core.word] = core to derivations
            }
        }

        wordsAndDataMap.values.forEach { (w, ds) ->
            ds.forEach {
                val (name, tags) = it.split("@")
                w.derivationClusterTemplate.typeToCore[DerivationType.valueOf(name)] =
                    parseDerivationTemplates(tags, wordsAndDataMap)
            }
        }

        baseWords.addAll(wordsAndDataMap.values.map { it.first }.sortedBy { it.word })
        allWords.addAll(allWords)
    }
}

fun parseSemanticsTagTemplates(string: String) = string
    .split(",")
    .map {
        val (name, prob) = it.split(":")
        SemanticsTagTemplate(name, prob.toDouble())
    }

fun getType(string: String) = when (string) {
    "G" -> genderName
    "A" -> animosityName
    "T" -> "transitivity"
    else -> if (string.length > 1)
        string
    else throw GeneratorException("Unknown SemanticsTag type alias $string")
}

fun parseDerivationTemplates(string: String, wordsAndDataMap: WordsAndDataMap) = string
    .split(",")
    .map {
        val (name, prob) = it.split(":")
        DerivationLink(wordsAndDataMap.getValue(name).first, prob.toDouble())
    }[0]//TODO to lists

private typealias WordsAndDataMap = Map<String, Pair<SemanticsCoreTemplate, List<String>>>
