package io.tashtabash.lang.containers

import io.tashtabash.lang.generator.util.DataConsistencyException
import io.tashtabash.lang.generator.util.GeneratorException
import io.tashtabash.lang.language.category.animosityName
import io.tashtabash.lang.language.category.nounClassName
import io.tashtabash.lang.language.derivation.DerivationType
import io.tashtabash.lang.language.lexis.*
import java.io.File


class WordBase(private val supplementPath: String) {
    val baseWords: MutableList<SemanticsCoreTemplate> = ArrayList()
    val allWords: MutableList<SemanticsCoreTemplate> = ArrayList()

    init {
        val wordsAndDataMap = mutableMapOf<String, UnparsedLinksTemplate>()
        val lines = readLines()

        lines.forEach { line ->
            val unparsedLinksTemplate = parseLine(line)
            if (wordsAndDataMap[unparsedLinksTemplate.template.word] != null)
                throw GeneratorException("Word ${unparsedLinksTemplate.template.word} already exists")

            wordsAndDataMap[unparsedLinksTemplate.template.word] = unparsedLinksTemplate
        }

        for (unparsedLinksTemplate in wordsAndDataMap.values)
            parseDerivations(unparsedLinksTemplate)

        for (unparsedLinksTemplate in wordsAndDataMap.values)
            parseCompounds(unparsedLinksTemplate, wordsAndDataMap)

        baseWords += wordsAndDataMap.values
            .map { it.template }
            .sortedBy { it.word }
        allWords += baseWords
    }

    private fun parseLine(line: String): UnparsedLinksTemplate {
        val tokens = line.split(" +".toRegex())

        val word = tokens[0]
        val speechPart = SpeechPart.valueOf(tokens[1])
        val realizationProbability = tokens[2].toDouble()

        val connotations = tokens.filter { it.contains("*") }
        val tags = tokens.filter { it.contains("|") }
        val derivations = tokens.filter { it.contains("@") }
        val compounds = tokens.filter { it.contains("&") }

        val core = SemanticsCoreTemplate(
            word,
            speechPart,
            parseConnotations(connotations),
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

        return UnparsedLinksTemplate(core, derivations, compounds)
    }

    private fun parseDerivations(unparsedLinksTemplate: UnparsedLinksTemplate) {
        for (derivation in unparsedLinksTemplate.derivations) {
            val (name, tags) = derivation.split("@")
            unparsedLinksTemplate.template.derivationClusterTemplate.typeToCore[DerivationType.valueOf(name)] =
                parseDerivationTemplates(tags).toMutableList()
        }
    }

    private fun parseCompounds(
        unparsedLinksTemplate: UnparsedLinksTemplate,
        wordsAndDataMap: MutableMap<String, UnparsedLinksTemplate>
    ) {
        for (compound in unparsedLinksTemplate.compounds)
            unparsedLinksTemplate.template.derivationClusterTemplate.possibleCompounds += parseCompoundTemplate(
                compound,
                wordsAndDataMap
            )
    }

    fun addWords(words: List<SemanticsCoreTemplate>) {
        baseWords += words
        allWords += words
    }

    private fun parseConnotations(connotations: List<String>) = Connotations(
        connotations.flatMap { it.split('*') }
            .filter { it.isNotBlank() }
            .map {
                val (name, strength) = it.split(':')
                Connotation(name, strength.toDouble())
            }
            .toSet()
    )

    private fun readLines(): List<String> {
        val semiLines = File("$supplementPath/Words")
            .readLines()
            .filter { it.isNotBlank() && it[0] != '/' }

        val lines = mutableListOf<String>()

        for (line in semiLines)
            lines += if (line[0].isWhitespace()) {
                val last = lines.last()
                lines.removeAt(lines.lastIndex)
                last + line
            } else line
        return lines
    }
}

fun parseSemanticsTagTemplates(string: String) = string.split(",")
    .map {
        if (it.contains(':')) {
            val (name, prob) = it.split(":")

            SemanticsTagTemplate(name, prob.toDouble())
        } else
            SemanticsTagTemplate(it)

    }

fun getType(string: String) = when (string) {
    "G" -> nounClassName
    "A" -> animosityName
    "T" -> "transitivity"
    else -> if (string.length <= 1)
        throw DataConsistencyException("Unknown SemanticsTag type alias $string")
    else string
}

fun getInstantiationType(string: String) = when (string) {
    "G", "A" -> false
    "T" -> true
    else ->
        if (string.length > 1)
            true
        else
            throw DataConsistencyException("Unknown SemanticsTag type alias $string")
}

private fun parseDerivationTemplates(string: String) = string.split(",")
    .map {
        val (name, prob) = it.split(":")
        DerivationLink(name, prob.toDouble())
    }


private fun parseCompoundTemplate(string: String, wordsAndDataMap: WordsAndDataMap): CompoundLink {
    val (names, prob) = string.split(":")
//    val cores = names.split("&").map { wordsAndDataMap.getValue(it).template }
    val cores = names.split("&")

    return CompoundLink(cores, prob.toDouble())
}


private typealias WordsAndDataMap = Map<String, UnparsedLinksTemplate>

private data class UnparsedLinksTemplate(
    val template: SemanticsCoreTemplate,
    val derivations: List<String>,
    val compounds: List<String>
)
