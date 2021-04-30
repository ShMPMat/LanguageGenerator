package shmp.lang.language

import shmp.lang.generator.util.GeneratorException
import shmp.lang.language.category.CategorySource
import shmp.lang.language.category.paradigm.SourcedCategoryValue
import shmp.lang.language.lexis.SpeechPart
import shmp.lang.language.lexis.Word
import shmp.lang.language.syntax.WordSequence
import shmp.lang.utils.listCartesianProduct


fun getParadigmPrinted(language: Language, word: Word, printOptionalCategories: Boolean = false): String {
    return "Base - $word\n" +
            listCartesianProduct(
                language.changeParadigm.wordChangeParadigm
                    .getSpeechPartParadigm(word.semanticsCore.speechPart)
                    .categories
                    .filter { if (printOptionalCategories) true else it.isCompulsory }
                    .map { it.actualSourcedValues }
            )
                .map { language.changeParadigm.wordChangeParadigm.apply(word, it) to it }
                .map { listOf("${it.first}", " - ") + it.second.joinToString(", &").split("&") }
                .lineUpAll()
                .sorted()
                .joinToString("\n")
}

fun getClauseAndInfoStr(wordSequence: WordSequence): String {
    val (words, infos) = wordSequence.words
        .map { it.toString() }
        .zip(getClauseInfoPrinted(wordSequence).split(" "))
        .map { (s1, s2) -> lineUp(s1, s2) }
        .map { (s1, s2) -> s1 to s2 }
        .unzip()
    return words.joinToString(" ") + "\n" +
            infos.joinToString(" ")
}

fun lineUp(ss: List<String>): List<String> {
    val max = ss
        .map { it.length }
        .maxOrNull()
        ?: throw GeneratorException("String list is empty")
    return ss.map { it + " ".repeat(max - it.length) }
}

fun lineUp(vararg ss: String) = lineUp(ss.toList())

fun List<List<String>>.lineUpAll(): List<String> {
    if (this.isEmpty()) {
        return emptyList()
    }

    return if (this[0].size == 1)
        lineUp(map { it[0] })
    else {
        val linedPostfixes = lineUp(this.map { it.last() })

        this.map { it.dropLast(1) }
            .lineUpAll()
            .mapIndexed { i, s -> s + linedPostfixes[i] }
    }
}

fun getClauseInfoPrinted(wordSequence: WordSequence) =
    wordSequence.words.joinToString(" ") { getWordInfoPrinted(it) }

fun getWordInfoPrinted(word: Word) = getSemanticsPrinted(word) +
        word.categoryValues
            .joinToString("") { "-" + it.smartPrint(word.categoryValues) }
            .replace(" ", ".")

private fun getSemanticsPrinted(word: Word) = word.syntaxRole?.short ?:
    if (word.semanticsCore.speechPart.type !in listOf(SpeechPart.Particle, SpeechPart.Article, SpeechPart.DeixisPronoun, SpeechPart.Adposition))
        word.semanticsCore.toString()
    else ""

fun SourcedCategoryValue.smartPrint(allValues: List<SourcedCategoryValue>): String {
    val allSources = allValues.groupBy { it.source }

    return if (allSources.size == 1 || allSources.size == 2 && allSources.containsKey(CategorySource.SelfStated))
        categoryValue.shortName
    else "$this"
}
