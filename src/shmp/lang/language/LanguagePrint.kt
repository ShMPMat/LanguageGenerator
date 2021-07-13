package shmp.lang.language

import shmp.lang.generator.util.GeneratorException
import shmp.lang.language.category.CategorySource
import shmp.lang.language.category.paradigm.SourcedCategoryValue
import shmp.lang.language.lexis.SpeechPart
import shmp.lang.language.lexis.Word
import shmp.lang.language.syntax.WordSequence
import shmp.lang.utils.listCartesianProduct


fun getParadigmPrinted(language: Language, word: Word, printOptionalCategories: Boolean = false) =
    "Base - $word\n" +
            listCartesianProduct(
                language.changeParadigm.wordChangeParadigm
                    .getSpeechPartParadigm(word.semanticsCore.speechPart)
                    .categories
                    .filter { if (printOptionalCategories) true else it.compulsoryData.isCompulsory }
                    .filter { !it.category.staticSpeechParts.contains(word.semanticsCore.speechPart.type) }
                    .map { it.actualSourcedValues }
            )
                .map { language.changeParadigm.wordChangeParadigm.apply(word, it) to it }
                .map { (ws, vs) ->
                    val categoryValues = vs.map { it.categoryValue }
                    val relevantCategories = vs
                        .filter { it.parent.compulsoryData.isApplicable(categoryValues) }

                    listOf("$ws", " - ") + relevantCategories.joinToString(", &").split("&")
                }
                .lineUpAll()
                .sorted()
                .distinct()
                .joinToString("\n")

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
    if (isEmpty())
        return emptyList()

    return if (maxOf { it.size } == 1)
        lineUp(map { if (it.isEmpty()) "" else it[0] })
    else {
        val linedPrefixes = lineUp(map { it.first() })

        map { if (it.isEmpty()) listOf("") else it.drop(1) }
            .lineUpAll()
            .mapIndexed { i, s -> linedPrefixes[i] + s }
    }
}

fun getClauseInfoPrinted(wordSequence: WordSequence) =
    wordSequence.words.joinToString(" ") { getWordInfoPrinted(it) }

fun getWordInfoPrinted(word: Word): String {
    val semantics = getSemanticsPrinted(word)
    val categories =  word.categoryValues
        .joinToString("") { "-" + it.smartPrint(word.categoryValues) }
        .replace(" ", ".")

    return if (semantics.isBlank())
        categories.drop(1)
    else
        semantics + categories

}

private fun getSemanticsPrinted(word: Word) = word.syntaxRole?.short ?:
    if (word.semanticsCore.speechPart.type !in listOf(SpeechPart.Particle, SpeechPart.Article, SpeechPart.DeixisPronoun, SpeechPart.Adposition))
        word.semanticsCore.toString()
    else ""

fun SourcedCategoryValue.smartPrint(allValues: List<SourcedCategoryValue>): String {
    val allSources = allValues.groupBy { it.source }

    return if (allSources.size == 1 || allSources.size == 2 && allSources.containsKey(CategorySource.SelfStated))
        categoryValue.alias
    else "$this"
}
