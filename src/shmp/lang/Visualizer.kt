package shmp.lang

import shmp.lang.containers.WordBase
import shmp.lang.generator.LanguageGenerator
import shmp.lang.language.Language
import shmp.lang.language.category.DeixisValue
import shmp.lang.language.category.NounClassValue
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

    private fun printTestSentences() {
        val firstSubj = NominalDescription(
            "mother",
            listOf(
                AdjectiveDescription("new"),
                PossessorDescription(NominalDescription("light", listOf(), ActorComplimentValue(AmountValue(1), null)))
            ),
            ActorComplimentValue(AmountValue(4), DeixisValue.Proximal)
        )
        val secondSubj = NominalDescription(
            "father",
            listOf(
                AdjectiveDescription("new"),
                AdjectiveDescription("high")
            ),
            ActorComplimentValue(AmountValue(2), DeixisValue.Undefined)
        )
        val thirdSubj = PronounDescription(
            "_personal_pronoun",
            listOf(),
            ActorType.Agent,
            ActorValue(PersonValue.First, NounClassValue.Female, AmountValue(1), DeixisValue.Proximal)
        )
        val firstObj = NominalDescription(
            "time",
            listOf(AdjectiveDescription("small")),
            ActorComplimentValue(AmountValue(1), DeixisValue.Medial)
        )
        val secondObj = PronounDescription(
            "_deixis_pronoun",
            listOf(),
            ActorType.Patient,
            ActorValue(PersonValue.Third, NounClassValue.Neutral, AmountValue(10), DeixisValue.ProximalAddressee)
        )
        val firstVerb = TransitiveVerbDescription("see", firstSubj, firstObj)
        val secondVerb = TransitiveVerbDescription("see", secondSubj, firstObj)
        val thirdVerb = TransitiveVerbDescription("see", thirdSubj, firstObj)
        val fourthVerb = TransitiveVerbDescription("see", thirdSubj, secondObj)

        val testSentencesMain = listOf(
            TransitiveVerbMainClauseDescription(firstVerb),
            TransitiveVerbMainClauseDescription(secondVerb),
            TransitiveVerbMainClauseDescription(thirdVerb),
            TransitiveVerbMainClauseDescription(fourthVerb)
        )
        val testSentencesCopula = listOf(
            CopulaMainClauseDescription(CopulaDescription(firstSubj, firstObj)),
            CopulaMainClauseDescription(CopulaDescription(secondSubj, firstObj)),
            CopulaMainClauseDescription(CopulaDescription(thirdSubj, firstObj)),
            PredicatePossessionDescription(firstSubj, firstObj)
        )

        val firstContext = Context(
            LongGonePast to Implicit,
            Simple to Explicit
        )
        val secondContext = Context(
            FarFuture to Implicit,
            GeneralQuestion to Explicit
//            mutableMapOf(ActorType.Agent to ActorValue(PersonValue.Second, GenderValue.Male, AmountValue(2)))
        )
        val fourthContext = Context(
            Regular to Implicit,
            Simple to Explicit
        )

        printSampleClause(testSentencesMain, firstContext, "Main")
        printSampleClause(testSentencesMain, secondContext, "General question")
        printSampleClause(testSentencesCopula, firstContext, "Copula")
        printSampleClause(testSentencesCopula, secondContext, "Copula question")
    }

    private fun printSampleClause(clauses: List<UnfoldableClauseDescription>, context: Context, comment: String) {
        println("$comment:")

        for (it in clauses)
            printSampleClause(it, context)

        println()
    }

    private fun printSampleClause(clause: UnfoldableClauseDescription, context: Context) {
        println(getClauseAndInfoStr(clause.unfold(language, context, Random(10))))
    }

    private fun printAdditionalLexisInfo() {
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
    val generator = LanguageGenerator("SupplementFiles", 216 + 27)
    val wordAmount = WordBase("SupplementFiles").baseWords.size

    Visualizer(generator.generateLanguage(wordAmount))
        .visualize()
}
