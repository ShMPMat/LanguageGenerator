package shmp.lang.containers

import shmp.lang.generator.util.DataConsistencyException
import shmp.lang.generator.util.GeneratorException
import shmp.lang.language.lexis.SpeechPart
import shmp.lang.language.category.animosityName
import shmp.lang.language.category.genderName
import shmp.lang.language.derivation.DerivationType
import shmp.lang.language.lexis.CompoundLink
import shmp.lang.language.lexis.DerivationLink
import java.io.File


class WordBase(private val supplementPath: String) {
    val baseWords: MutableList<SemanticsCoreTemplate> = ArrayList()
    val allWords: MutableList<SemanticsCoreTemplate> = ArrayList()

    init {
        val wordsAndDataMap = mutableMapOf<String, UnparsedLinksTemplate>()
        val lines = readLines()

        lines.forEach { line ->
            val tokens = line.split(" +".toRegex())

            val word = tokens[0]
            val speechPart = SpeechPart.valueOf(tokens[1])
            val realizationProbability = tokens[2].toDouble()

            val tags = tokens.filter { it.contains("|") }
            val derivations = tokens.filter { it.contains("@") }
            val compounds = tokens.filter { it.contains("&") }

            val core = SemanticsCoreTemplate(
                word,
                speechPart,
                tags.map {
                    val (name, semanticTags) = it.split("|")
                    SemanticsTagCluster(
                        parseSemanticsTagTemplates(semanticTags),
                        getType(name),
                        getInstantiationType(name)
                    )
                }.toSet(),
                DerivationClusterTemplate(),
                realizationProbability
            )

            if (wordsAndDataMap[core.word] != null)
                throw GeneratorException("Word ${core.word} already exists")

            wordsAndDataMap[core.word] = UnparsedLinksTemplate(core, derivations, compounds)
        }

        for (data in wordsAndDataMap.values)
            for (derivation in data.derivations) {
                val (name, tags) = derivation.split("@")
                data.template.derivationClusterTemplate.typeToCore[DerivationType.valueOf(name)] =
                    parseDerivationTemplates(tags, wordsAndDataMap)
            }

        for (data in wordsAndDataMap.values)
            for (compound in data.compounds)
                data.template.derivationClusterTemplate.possibleCompounds.add(
                    parseCompoundTemplate(compound, wordsAndDataMap)
                )

        baseWords.addAll(wordsAndDataMap.values.map { it.template }.sortedBy { it.word })
        allWords.addAll(baseWords)


    }

    private fun readLines(): List<String> {
        val semiLines = File("$supplementPath/Words")
            .readLines()
            .filter { !it.isBlank() && it[0] != '/' }

        val lines = mutableListOf<String>()

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
    else -> if (string.length <= 1)
        throw DataConsistencyException("Unknown SemanticsTag type alias $string")
    else string
}

fun getInstantiationType(string: String) = when (string) {
    "G" -> false
    "A" -> false
    "T" -> true
    else ->
        if (string.length > 1)
            true
        else
            throw DataConsistencyException("Unknown SemanticsTag type alias $string")
}

private fun parseDerivationTemplates(string: String, wordsAndDataMap: WordsAndDataMap) = string
    .split(",")
    .map {
        val (name, prob) = it.split(":")
        DerivationLink(wordsAndDataMap.getValue(name).template, prob.toDouble())
    }


private fun parseCompoundTemplate(string: String, wordsAndDataMap: WordsAndDataMap): CompoundLink {
    val (names, prob) = string.split(":")
    val cores = names.split("&").map { wordsAndDataMap.getValue(it).template }

    return CompoundLink(cores, prob.toDouble())
}


private typealias WordsAndDataMap = Map<String, UnparsedLinksTemplate>

private data class UnparsedLinksTemplate(
    val template: SemanticsCoreTemplate,
    val derivations: List<String>,
    val compounds: List<String>
)
