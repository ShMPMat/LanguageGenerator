package shmp

import shmp.containers.WordBase
import shmp.generator.LanguageGenerator
import shmp.language.CategoryValue
import shmp.language.Language
import shmp.language.category.CategorySource
import shmp.language.category.NumbersValue
import shmp.language.getClauseAndInfoStr
import shmp.language.lexis.Word
import shmp.language.syntax.*
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
        val subjAdj = language.getWord("new").toNode(language)
        val subj = language.getWord("mother").toNode(language, listOf(NumbersValue.Plural))
//        val subjAdj2 = language.getWord("small").toNode(language)

        val verb = language.getWord("have").toNode(language)

        val objAdj = language.getWord("new").toNode(language)
        val obj = language.getWord("time").toNode(language)

        subjAdj.setRelation(SyntaxRelation.Subject, subj)
        subj.setRelation(SyntaxRelation.Definition, subjAdj)
//        subjAdj2.setRelation(SyntaxRelation.Subject, subj)
//        subj.setRelation(SyntaxRelation.Definition, subjAdj2)
        subj.setRelation(SyntaxRelation.Verb, verb)

        verb.setRelation(SyntaxRelation.Subject, subj)
        verb.setRelation(SyntaxRelation.Object, obj)

        objAdj.setRelation(SyntaxRelation.Subject, obj)
        obj.setRelation(SyntaxRelation.Definition, objAdj)
        obj.setRelation(SyntaxRelation.Verb, verb)

        val testSentenceMain = Sentence(verb, SentenceType.MainClause)
        val testSentenceQuestion = Sentence(verb, SentenceType.Question)
        println("Main:")
        println(getClauseAndInfoStr(language.sentenceChangeParadigm.apply(testSentenceMain, Random(10))))
        println("General question:")
        println(getClauseAndInfoStr(language.sentenceChangeParadigm.apply(testSentenceQuestion, Random(10))))
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
    val generator = LanguageGenerator("SupplementFiles", 213)
    val wordAmount = WordBase("SupplementFiles").baseWords.size

    Visualizer(generator.generateLanguage(wordAmount))
        .visualize()
}
