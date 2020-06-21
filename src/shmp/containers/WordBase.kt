package shmp.containers

import shmp.generator.DataConsistencyException
import shmp.language.SpeechPart
import shmp.language.category.animosityName
import shmp.language.category.genderName
import shmp.language.derivation.DerivationType
import shmp.language.lexis.DerivationLink
import java.io.File

class WordBase(private val supplementPath: String) {
    val baseWords: MutableList<SemanticsCoreTemplate> = ArrayList()
    val allWords: MutableList<SemanticsCoreTemplate> = ArrayList()

    init {
        val wordsAndDataMap = mutableMapOf<String, Pair<SemanticsCoreTemplate, List<String>>>()
        val lines = readLines()

        lines.forEach { line ->
            val tokens = line.split(" +".toRegex())
            val word = tokens[0]
            val speechPart = SpeechPart.valueOf(tokens[1])
            val realizationProbability = tokens[2].toDouble()
            val tags = tokens.filter { it.contains("|") }
            val derivations = tokens.filter { it.contains("@") }

            val core = SemanticsCoreTemplate(
                word,
                speechPart,
                tags.map {
                    val (name, semanticTags) = it.split("|")
                    SemanticsTagCluster(
                        parseSemanticsTagTemplates(semanticTags),
                        getType(name)
                    )
                }.toSet(),
                DerivationClusterTemplate(),
                realizationProbability
            )
            wordsAndDataMap[core.word] = core to derivations
        }

        wordsAndDataMap.values.forEach { (w, ds) ->
            ds.forEach {
                val (name, tags) = it.split("@")
                w.derivationClusterTemplate.typeToCore[DerivationType.valueOf(name)] =
                    parseDerivationTemplates(tags, wordsAndDataMap)
            }
        }

        baseWords.addAll(wordsAndDataMap.values.map { it.first }.sortedBy { it.word })
        allWords.addAll(baseWords)
    }

    private fun readLines(): List<String> {
        val semiLines = File("$supplementPath/Words")
            .readLines()
            .filter { !it.isBlank() && it[0] != '/' }


        val lines = mutableListOf(semiLines[0])

        for (line in semiLines)
            lines.add(
                if (line[0].isWhitespace()) {
                    val last = lines.last()
                    lines.removeAt(lines.lastIndex)
                    last + line
                } else line
            )
        return lines
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
    else throw DataConsistencyException("Unknown SemanticsTag type alias $string")
}

fun parseDerivationTemplates(string: String, wordsAndDataMap: WordsAndDataMap) = string
    .split(",")
    .map {
        val (name, prob) = it.split(":")
        DerivationLink(wordsAndDataMap.getValue(name).first, prob.toDouble())
    }

private typealias WordsAndDataMap = Map<String, Pair<SemanticsCoreTemplate, List<String>>>
