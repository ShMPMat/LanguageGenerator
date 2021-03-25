package shmp.lang

import shmp.lang.containers.WordBase
import shmp.lang.generator.LanguageGenerator
import shmp.lang.language.Language
import shmp.lang.language.category.GenderValue
import shmp.lang.language.category.NumbersValue
import shmp.lang.language.category.PersonValue
import shmp.lang.language.getClauseAndInfoStr
import shmp.lang.language.lexis.Word
import shmp.lang.language.syntax.clause.description.*
import shmp.lang.language.syntax.context.ActorType
import shmp.lang.language.syntax.context.Context
import shmp.lang.language.syntax.context.ContextValue.*
import shmp.lang.language.syntax.context.ContextValue.TimeContext.*
import shmp.lang.language.syntax.context.ContextValue.TypeContext.GeneralQuestion
import shmp.lang.language.syntax.context.ContextValue.TypeContext.Simple
import shmp.lang.language.syntax.context.Priority.Explicit
import shmp.lang.language.syntax.context.Priority.Implicit
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
        val thirdSubj = PersonalPronounDescription(
            listOf(),
            ActorType.Agent
        )
        val obj = NominalDescription(
            "time",
            listOf(AdjectiveDescription("small"))
        )
        val firstVerb = TransitiveVerbDescription("have", firstSubj, obj)
        val secondVerb = TransitiveVerbDescription("have", secondSubj, obj)
        val thirdVerb = TransitiveVerbDescription("have", thirdSubj, obj)

        val testSentencesMain = listOf(
            TransitiveVerbMainClauseDescription(firstVerb),
            TransitiveVerbMainClauseDescription(secondVerb),
            TransitiveVerbMainClauseDescription(thirdVerb),
        )
        val testSentencesCopula = listOf(
            CopulaMainClauseDescription(CopulaDescription(firstSubj, obj)),
            CopulaMainClauseDescription(CopulaDescription(secondSubj, obj)),
            CopulaMainClauseDescription(CopulaDescription(thirdSubj, obj))
        )

        val firstContext = Context(
            LongGonePast to Implicit,
            Simple to Explicit,
            mapOf(ActorType.Agent to ActorValue(PersonValue.First, GenderValue.Female, AmountValue(1)))
        )
        val secondContext = Context(
            FarFuture to Implicit,
            GeneralQuestion to Explicit,
            mapOf(ActorType.Agent to ActorValue(PersonValue.Second, GenderValue.Male, AmountValue(2)))
        )
        val fourthContext = Context(
            Regular to Implicit,
            Simple to Explicit,
            mapOf(ActorType.Agent to ActorValue(PersonValue.First, GenderValue.Female, AmountValue(1)))
        )

        printSampleClause(testSentencesMain, firstContext, "Main")
        printSampleClause(testSentencesMain, secondContext, "General question")
        printSampleClause(testSentencesCopula, firstContext, "Copula")
        printSampleClause(testSentencesCopula, secondContext, "Copula question")
    }

    fun printSampleClause(clauses: List<UnfoldableClauseDescription>, context: Context, comment: String) {
        println("$comment:")

        for (it in clauses)
            printSampleClause(it, context)

        println()
    }

    fun printSampleClause(clause: UnfoldableClauseDescription, context: Context) {
        println(
            getClauseAndInfoStr(
                clause.unfold(language, context, Random(10))
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
    val generator = LanguageGenerator("SupplementFiles", 216 + 14)
    val wordAmount = WordBase("SupplementFiles").baseWords.size

    Visualizer(generator.generateLanguage(wordAmount))
        .visualize()
}
