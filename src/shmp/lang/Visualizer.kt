package shmp.lang

import shmp.lang.containers.WordBase
import shmp.lang.generator.LanguageGenerator
import shmp.lang.language.Language
import shmp.lang.language.category.NumbersValue
import shmp.lang.language.getClauseAndInfoStr
import shmp.lang.language.lexis.Word
import shmp.lang.language.syntax.clause.description.*
import kotlin.random.Random


class Visualizer(val language: Language) {
    fun visualize() {
        printTestSentences()
        print("\n\n")
        print(language)
        println()
        printAdditionalLexisInfo()
    }

    fun printTestSentences() {
        val firstSubj = NominalDescription(
            "mother",
            listOf(
                AdjectiveDescription("new"),
                AdjectiveDescription("high")
            ),
            listOf(NumbersValue.Plural)
        )
        val secondSubj = NominalDescription(
            "father",
            listOf(
                AdjectiveDescription("new"),
                AdjectiveDescription("high")
            )
        )
        val obj = NominalDescription(
            "time",
            listOf(AdjectiveDescription("small"))
        )
        val firstVerb = TransitiveVerbDescription("have", firstSubj, obj)
        val secondVerb = TransitiveVerbDescription("have", secondSubj, obj)

        val testSentencesMain = listOf(
            TransitiveVerbMainClauseDescription(firstVerb),
            TransitiveVerbMainClauseDescription(secondVerb)
        )
        val testSentencesQuestion = listOf(
            TransitiveVerbQuestionDescription(firstVerb),
            TransitiveVerbQuestionDescription(secondVerb)
        )
        val testSentencesCopula = listOf(
            CopulaMainClauseDescription(CopulaDescription(firstSubj, obj)),
            CopulaMainClauseDescription(CopulaDescription(secondSubj, obj))
        )
        val testSentencesCopulaQuestion = listOf(
            CopulaQuestionDescription(CopulaDescription(firstSubj, obj)),
            CopulaQuestionDescription(CopulaDescription(secondSubj, obj))
        )

        printSampleClause(testSentencesMain, "Main")
        printSampleClause(testSentencesQuestion, "General question")
        printSampleClause(testSentencesCopula, "Copula")
        printSampleClause(testSentencesCopulaQuestion, "Copula question")
    }

    fun printSampleClause(clauses: List<UnfoldableClauseDescription>, comment: String) {
        println("$comment:")

        for (it in clauses)
            printSampleClause(it)

        println()
    }

    fun printSampleClause(clause: UnfoldableClauseDescription) {
        println(
            getClauseAndInfoStr(
                clause.unfold(language, Random(10))
            )
        )
    }

    fun printAdditionalLexisInfo() {
        val synonyms = language.lexis.words
            .groupBy { it.semanticsCore.toString() }
            .map { it.value }
            .filter { it.size > 1 }

        print(
            """
        |Synonyms:
        |${synonyms.joinToString("\n") { "$it - ${it[0].semanticsCore}" }}
        |
        |Lexis size - ${language.lexis.size} words
        |
        |Derivations:
        |${
                language.lexis.words
                    .filter { it.semanticsCore.changeHistory != null }
                    .sortedBy { it.semanticsCore.changeDepth }
                    .joinToString("\n\n") { printDerivationStory(it) }
            }
        |
        |Collapsed meanings:
        |${
                language.lexis.words
                    .filter { it.semanticsCore.meaningCluster.size > 1 }
                    .joinToString("\n") { "$it - ${it.semanticsCore.meaningCluster}" }
            }
        |
    """.trimMargin()
        )
    }

    private fun printDerivationStory(word: Word) = word.semanticsCore.changeHistory?.printHistory(word)
        ?: "No derivations"
}

fun main() {
    val generator = LanguageGenerator("SupplementFiles", 216 + 4)
    val wordAmount = WordBase("SupplementFiles").baseWords.size

    Visualizer(generator.generateLanguage(wordAmount))
        .visualize()
}
