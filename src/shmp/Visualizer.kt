package shmp

import shmp.containers.WordBase
import shmp.generator.LanguageGenerator
import shmp.language.Language
import shmp.language.category.NumbersValue
import shmp.language.getClauseAndInfoStr
import shmp.language.lexis.Word
import shmp.language.syntax.*
import shmp.language.syntax.clause.*
import kotlin.random.Random


data class Visualizer(val language: Language) {
    fun visualize() {
        printTestSentences()
        print("\n\n")
        print(language)
        println()
        printAdditionalLexisInfo()
    }

    fun printTestSentences() {
        val subj = NominalClause(
            language.getWord("mother"),
            listOf(AdjectiveClause(language.getWord("new"))),
            listOf(NumbersValue.Plural)
        )
        val obj = NominalClause(
            language.getWord("time"),
            listOf(AdjectiveClause(language.getWord("small")))
        )

        val verb = TransitiveVerbClause(
            language.getWord("have"),
            subj,
            obj
        )

        val testSentenceMain = TransitiveVerbMainSentence(verb)
        val testSentenceQuestion = TransitiveVerbQuestion(verb)
        println("Main:")
        println(getClauseAndInfoStr(language.sentenceChangeParadigm.apply(
            testSentenceMain,
            language,
            Random(10)
        )))
        println("General question:")
        println(getClauseAndInfoStr(language.sentenceChangeParadigm.apply(
            testSentenceQuestion,
            language,
            Random(10)
        )))
    }

    fun printAdditionalLexisInfo() {
        val synonyms = language.words
            .groupBy { it.semanticsCore.toString() }
            .map { it.value }
            .filter { it.size > 1 }

        print(
            """
        |Synonyms:
        |${synonyms.joinToString("\n") { "$it - ${it[0].semanticsCore}" }}
        |
        |Lexis size - ${language.words.size} words
        |
        |Derivations:
        |${language.words
                .filter { it.semanticsCore.changeHistory != null }
                .sortedBy { it.semanticsCore.changeDepth }
                .joinToString("\n\n") { printDerivationStory(it) }
            }
        |
        |Collapsed meanings:
        |${language.words
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
    val generator = LanguageGenerator("SupplementFiles", 214)
    val wordAmount = WordBase("SupplementFiles").baseWords.size

    Visualizer(generator.generateLanguage(wordAmount))
        .visualize()
}
