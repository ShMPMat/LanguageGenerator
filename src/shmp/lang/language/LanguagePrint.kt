package shmp.lang.language

import shmp.lang.generator.util.GeneratorException
import shmp.lang.language.category.CategorySource
import shmp.lang.language.category.paradigm.SourcedCategoryValue
import shmp.lang.language.lexis.SpeechPart
import shmp.lang.language.lexis.Word
import shmp.lang.language.syntax.SyntaxRelation
import shmp.lang.language.syntax.WordSequence
import shmp.lang.language.syntax.arranger.PassingArranger
import shmp.lang.language.syntax.clause.realization.wordToNode
import shmp.lang.language.syntax.clause.translation.SentenceClauseTranslator
import shmp.lang.utils.listCartesianProduct
import kotlin.random.Random


fun Language.getNumeralsPrinted() = when(changeParadigm.numeralParadigm.base) {
    NumeralSystemBase.Decimal -> {
        printNumerals((1..121).toList() + listOf(200, 300, 400, 500, 600, 700, 800, 900, 1000, 10000))
    }
    NumeralSystemBase.Restricted3 -> printNumeralsRange(1..4)
    NumeralSystemBase.Restricted5 -> printNumeralsRange(1..6)
    NumeralSystemBase.Restricted20 -> printNumeralsRange(1..21)
}

private fun Language.printNumeralsRange(range: IntRange) = printNumerals(range.toList())

private fun Language.printNumerals(numbers: List<Int>) = numbers
    .map { changeParadigm.numeralParadigm.constructNumeral(it, lexis) }
    .map { n ->
        changeParadigm.wordChangeParadigm.getDefaultState(n.word)
            .groupBy { it.source }
            .forEach { (s, vs) ->
                val pureVs = vs.map { it.categoryValue }

                when(s) {
                    CategorySource.SelfStated -> n.insertCategoryValues(pureVs)
                    is CategorySource.RelationGranted -> {
                        val dummyWord = lexis.words.first { it.semanticsCore.speechPart.type in s.possibleSpeechParts }
                        val dummyNode = dummyWord.wordToNode(s.relation, PassingArranger, pureVs)

                        dummyNode.setRelationChild(SyntaxRelation.AdNumeral, n)
                    }
                }
            }
        SentenceClauseTranslator(changeParadigm).applyNode(n, Random(0))
    }
    .map { listOf(it.toString(), " - " + it.getClauseInfoPrinted()) }
    .lineUpAll()
    .joinToString("\n")


fun Language.getParadigmPrinted(word: Word, printOptionalCategories: Boolean = false) =
    "Base - $word\n" +
            listCartesianProduct(
                changeParadigm.wordChangeParadigm
                    .getSpeechPartParadigm(word.semanticsCore.speechPart)
                    .categories
                    .filter { if (printOptionalCategories) true else it.compulsoryData.isCompulsory }
                    .filter { !it.category.staticSpeechParts.contains(word.semanticsCore.speechPart.type) }
                    .map { it.actualSourcedValues }
            )
                .map { changeParadigm.wordChangeParadigm.apply(word, it) to it }
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

    return if (allSources.size == 1 || allSources.size == 2 && allSources.containsKey(CategorySource.SelfStated))
        categoryValue.alias
    else "$this"
}
