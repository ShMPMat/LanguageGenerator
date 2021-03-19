package shmp

import shmp.containers.WordBase
import shmp.generator.LanguageGenerator
import shmp.language.Language
import shmp.language.category.NumbersValue
import shmp.language.getClauseAndInfoStr
import shmp.language.lexis.Word
import shmp.language.syntax.clause.description.*
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
        val subj = NominalDescription(
            "mother",
            listOf(
                AdjectiveDescription("new"),
                AdjectiveDescription("small")
            ),
            listOf(NumbersValue.Plural)
        )
        val obj = NominalDescription(
            "time",
            listOf(AdjectiveDescription("small"))
        )
        val verb = TransitiveVerbDescription("have", subj, obj)

        val testSentenceMain = TransitiveVerbMainClauseDescription(verb)
        val testSentenceQuestion = TransitiveVerbQuestionDescription(verb)
        val testSentenceCopula = CopulaMainClauseDescription(CopulaDescription(subj, obj))
        val testSentenceCopulaQuestion = CopulaQuestionDescription(CopulaDescription(subj, obj))

        printSampleClause(testSentenceMain, "Main")
        printSampleClause(testSentenceQuestion, "General question")
        printSampleClause(testSentenceCopula, "Copula")
        printSampleClause(testSentenceCopulaQuestion, "Copula question")
    }

    fun printSampleClause(clause: UnfoldableClauseDescription, comment: String) {
        println("$comment:")
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
    val generator = LanguageGenerator("SupplementFiles", 216 + 2)
    val wordAmount = WordBase("SupplementFiles").baseWords.size

    Visualizer(generator.generateLanguage(wordAmount))
        .visualize()
}
