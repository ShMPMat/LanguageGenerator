package io.tashtabash.lang.language

import io.tashtabash.lang.generator.util.GeneratorException
import io.tashtabash.lang.language.category.CategorySource
import io.tashtabash.lang.language.category.paradigm.SourcedCategoryValue
import io.tashtabash.lang.language.lexis.SpeechPart
import io.tashtabash.lang.language.lexis.Word
import io.tashtabash.lang.language.syntax.SyntaxRelation
import io.tashtabash.lang.language.syntax.sequence.WordSequence
import io.tashtabash.lang.language.syntax.arranger.PassingArranger
import io.tashtabash.lang.language.syntax.clause.realization.wordToNode
import io.tashtabash.lang.language.syntax.clause.translation.SentenceClauseTranslator
import kotlin.random.Random


fun Language.getNumeralsPrinted() = when(changeParadigm.numeralParadigm.base) {
    NumeralSystemBase.Decimal -> printNumerals(
        (1..121).toList() + listOf(200, 300, 400, 500, 600, 700, 800, 900, 1000, 10000)
    )
    NumeralSystemBase.Vigesimal -> printNumerals(
        (1..421).toList() + listOf(800, 1200, 1600, 2000, 2400, 2800, 3200, 3600, 4000)
    )
    NumeralSystemBase.VigesimalTill100 -> printNumerals(
        (1..121).toList() + listOf(200, 300, 400, 500, 600, 700, 800, 900, 1000, 10000)
    )
    NumeralSystemBase.SixtyBased -> printNumerals(
        (1..100).toList() + listOf(200, 300, 400, 500, 600, 1000, 3600, 36000, 40000)
    )
    NumeralSystemBase.Restricted3 -> printNumeralsRange(1..4)
    NumeralSystemBase.Restricted5 -> printNumeralsRange(1..6)
    NumeralSystemBase.Restricted10 -> printNumeralsRange(1..11)
    NumeralSystemBase.Restricted20 -> printNumeralsRange(1..21)
}

private fun Language.printNumeralsRange(range: IntRange) = printNumerals(range.toList())

private fun Language.printNumerals(numbers: List<Int>) = numbers
    .map { it to changeParadigm.numeralParadigm.constructNumeral(it, lexis) }
    .map { (num,node) ->
        changeParadigm.wordChangeParadigm.getDefaultState(node.word)
            .groupBy { it.source }
            .forEach { (s, vs) ->
                val pureVs = vs.map { it.categoryValue }

                when(s) {
                    CategorySource.Self -> node.addCategoryValues(pureVs)
                    is CategorySource.Agreement -> {
                        val dummyWord = lexis.words.first { it.semanticsCore.speechPart.type in s.possibleSpeechParts }
                        val dummyNode = dummyWord.wordToNode(s.relation, pureVs, PassingArranger)

                        dummyNode.setRelationChild(SyntaxRelation.AdNumeral, node)
                    }
                }
            }
        num to SentenceClauseTranslator(changeParadigm).applyNode(node, Random(0))
    }
    .map { (n, c) -> listOf("$n  ", c.toString(), " - " + c.getClauseInfoPrinted()) }
    .lineUpAll()
    .joinToString("\n")


fun Language.printParadigm(word: Word, printOptionalCategories: Boolean = false): String {
    val allWordForms = changeParadigm.wordChangeParadigm.getAllWordForms(word, printOptionalCategories)

    return "Base - $word\n" +
            allWordForms
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
}

fun getClauseAndInfoStr(wordSequence: WordSequence): String {
    val (words, infos) = wordSequence.words
        .map { it.toString() }
        .zip(wordSequence.getClauseInfoPrinted().split(" "))
        .map { (s1, s2) -> lineUp(s1, s2) }
        .map { (s1, s2) -> s1 to s2 }
        .unzip()
    return words.joinToString(" ") + "\n" +
            infos.joinToString(" ")
}

fun List<String>.lineUp(): List<String> {
    val max = map { it.length }
        .maxOrNull()
        ?: throw GeneratorException("String list is empty")
    return map { it + " ".repeat(max - it.length) }
}

fun lineUp(vararg ss: String) = ss.toList().lineUp()

fun List<List<String>>.lineUpAll(): List<String> {
    if (isEmpty())
        return emptyList()

    return if (maxOf { it.size } == 1)
        map { if (it.isEmpty()) "" else it[0] }.lineUp()
    else {
        val linedPrefixes = map { it.first() }.lineUp()

        map { if (it.isEmpty()) listOf("") else it.drop(1) }
            .lineUpAll()
            .mapIndexed { i, s -> linedPrefixes[i] + s }
    }
}

fun WordSequence.getClauseInfoPrinted() = words.joinToString(" ") { getWordInfoPrinted(it) }

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

    return if (allSources.size == 1 || allSources.size == 2 && allSources.containsKey(CategorySource.Self))
        categoryValue.alias
    else "$this"
}
