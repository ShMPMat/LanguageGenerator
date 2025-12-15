package io.tashtabash.lang.language

import io.tashtabash.lang.generator.util.GeneratorException
import io.tashtabash.lang.language.category.CategorySource
import io.tashtabash.lang.language.category.paradigm.SourcedCategoryValue
import io.tashtabash.lang.language.lexis.Lexis
import io.tashtabash.lang.language.lexis.SpeechPart
import io.tashtabash.lang.language.lexis.Word
import io.tashtabash.lang.language.morphem.MorphemeData
import io.tashtabash.lang.language.syntax.SyntaxRelation
import io.tashtabash.lang.language.syntax.sequence.WordSequence
import io.tashtabash.lang.language.syntax.arranger.PassingArranger
import io.tashtabash.lang.language.syntax.clause.realization.toNode
import io.tashtabash.lang.language.syntax.clause.syntax.SyntaxNodeTranslator
import kotlin.random.Random


val shortSemanticsMap = mapOf(
    "_personal_pronoun" to "PP",
    "_deixis_pronoun" to "DEM"
)

fun Language.getNumeralsPrinted() = when (changeParadigm.numeralParadigm.base) {
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
    .map { (num, node) ->
        changeParadigm.wordChangeParadigm.getDefaultState(node.word)
            .groupBy { it.source }
            .forEach { (s, vs) ->
                val pureVs = vs.map { it.categoryValue }

                when (s) {
                    CategorySource.Self -> node.categoryValues += pureVs
                    is CategorySource.Agreement -> {
                        val dummyWord = lexis.words.first { it.semanticsCore.speechPart.type in s.possibleSpeechParts }
                        val dummyNode = dummyWord.toNode(s.relation, pureVs, PassingArranger)

                        dummyNode.setRelationChild(SyntaxRelation.AdNumeral, node)
                    }
                }
            }
        num to SyntaxNodeTranslator(changeParadigm).applyNodeFully(node, Random(0))
    }
    .map { (n, c) -> listOf("$n  ", c.toString(), " - " + c.printClauseInfo(true)) }
    .lineUpAll()
    .joinToString("\n")

fun Language.printParadigm(speechPart: SpeechPart, number: Int = 1, printOptionalCategories: Boolean = true): String =
    lexis.words.filter { it.semanticsCore.speechPart.type == speechPart }
        .take(number)
        .joinToString("\n") { printParadigm(it, printOptionalCategories) }

fun Language.printParadigm(word: Word, printOptionalCategories: Boolean = true): String {
    val allWordForms = changeParadigm.wordChangeParadigm.getAllWordForms(word, printOptionalCategories)

    return "Base - ${word.getPhoneticRepresentation()} (${word.semanticsCore.meaningCluster})\n" +
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

fun Lexis.printSpeechParts(): String =
    words.groupBy { it.semanticsCore.speechPart }
        .map { (speechPart, words) ->
            "$speechPart: " +
                    words.take(10).joinToString { "${it.getPhoneticRepresentation()} - ${it.semanticsCore}" }
        }.joinToString("\n")

fun getClauseAndInfoStr(wordSequence: WordSequence, printDerivation: Boolean = true): String {
    val glosses = wordSequence.printClauseInfo(printDerivation)
        .split(" ")
        .map { "$it " }
    val morphemes = wordSequence.printClauseMorphemes(printDerivation)
        .split(" ")
        .map { "$it " }
    val words = wordSequence.words
        .map { it.getPhoneticRepresentation() }
        .map { "$it " }

    return listOf(words, morphemes, glosses).lineUpAll()
        .joinToString("\n")
}

fun List<String>.lineUp(): List<String> {
    val max = maxOfOrNull { getStrWidth(it) }
        ?: throw GeneratorException("String list is empty")

    return map { it + " ".repeat(max - getStrWidth(it)) }
}

fun lineUp(vararg ss: String) = ss.toList().lineUp()

fun List<List<String>>.lineUpAll(): List<String> {
    if (isEmpty())
        return emptyList()

    return if (maxOf { it.size } == 1)
        map { if (it.isEmpty()) "" else it[0] }
            .lineUp()
    else {
        val linedPrefixes = map { it.firstOrNull() ?: "" }
            .lineUp()

        map { if (it.isEmpty()) listOf("") else it.drop(1) }
            .lineUpAll()
            .mapIndexed { i, s -> linedPrefixes[i] + s }
    }
}

fun getStrWidth(str: String): Int = str
    .replace("\\p{M}".toRegex(), "")
    .length

fun WordSequence.printClauseInfo(printDerivation: Boolean) = words.joinToString(" ") {
    printMorphemeInfo(it, printDerivation)
}

fun WordSequence.printClauseMorphemes(printDerivation: Boolean) = words.joinToString(" ") {
    printWordMorphemes(it, printDerivation)
}

fun printWordMorphemes(word: Word, printDerivation: Boolean = false): String {
    val morphemes =
        if (printDerivation)
            word.morphemes
        else
            mergeDerivationMorphemes(word.morphemes)
    var morphemeEndIdx = 0
    var symbolEndIdx = 0
    val phonemes = word.phonemes
    val symbols = word.toString()

    return morphemes.joinToString("-") { (size, categoryValues) ->
        morphemeEndIdx += size

        val morphemeSymbols = phonemes.subList(morphemeEndIdx - size, morphemeEndIdx)
            .joinToString("")
        val symbolStartIdx = symbolEndIdx
        symbolEndIdx += morphemeSymbols.length
        var resultSymbols = symbols.substring(symbolStartIdx, symbolEndIdx)

        while (symbols.length != symbolEndIdx
            && getStrWidth(symbols.substring(symbolStartIdx, symbolEndIdx + 1)) <= getStrWidth(morphemeSymbols)) {
            symbolEndIdx++
            resultSymbols = symbols.substring(symbolStartIdx, symbolEndIdx)
        }

        if (morphemeSymbols.isEmpty() && categoryValues.isNotEmpty())
            "Ø"
        else
            resultSymbols
    }
}

fun printMorphemeInfo(word: Word, printDerivation: Boolean): String {
    val morphemes =
        if (printDerivation)
            word.morphemes
        else
            mergeDerivationMorphemes(word.morphemes)

    return morphemes
        .joinToString("-") { (_, categoryValues, isRoot, derivationValues) ->
            var printedMorphemeData = categoryValues
                .joinToString(".") { it.smartPrint(word.categoryValues).replace(".", "_") }

            if (printDerivation) {
                if (printedMorphemeData.isNotEmpty() && derivationValues.isNotEmpty())
                    printedMorphemeData += "."

                printedMorphemeData += derivationValues
                    .joinToString { it.shortName }
            }

            val printedSemantics = getSemanticsPrinted(word)
                .let { shortSemanticsMap.getOrDefault(it, it) }

            val printedRootData = if (printedMorphemeData.isEmpty())
                ""
            else if (printedSemantics.isEmpty())
                printedMorphemeData
            else
                "($printedMorphemeData)"

            if (isRoot)
                printedSemantics + printedRootData
            else
                printedMorphemeData
        }
}

private fun mergeDerivationMorphemes(morphemes: List<MorphemeData>): List<MorphemeData> {
    var i = 1
    val resultMorphemes = morphemes.map { it }
        .toMutableList()

    while (i < resultMorphemes.size) {
        val curMorpheme = resultMorphemes[i]
        val prevMorpheme = resultMorphemes[i - 1]

        if (isMorphemeMergeable(curMorpheme) && isMorphemeMergeable(prevMorpheme)) {
            val newMorpheme = prevMorpheme + curMorpheme
            resultMorphemes.removeAt(i)
            resultMorphemes[i - 1] = newMorpheme
        } else
            i++
    }

    return resultMorphemes
}

private fun isMorphemeMergeable(morpheme: MorphemeData): Boolean =
    morpheme.categoryValues.isEmpty() && morpheme.derivationValues.isNotEmpty()
            || morpheme.isRoot

private fun getSemanticsPrinted(word: Word) =
        if (word.semanticsCore.speechPart.type !in nonSemanticSpeechParts)
            word.semanticsCore.toString()
        else ""

fun SourcedCategoryValue.smartPrint(allValues: List<SourcedCategoryValue>): String {
    val allSources = allValues.groupBy { it.source }

    return if (allSources.size == 1 || allSources.size == 2 && allSources.containsKey(CategorySource.Self))
        categoryValue.alias
    else "$this"
}

private val nonSemanticSpeechParts = listOf(
    SpeechPart.Particle,
    SpeechPart.Article,
    SpeechPart.DeixisPronoun,
    SpeechPart.Adposition
)
