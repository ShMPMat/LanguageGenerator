package io.tashtabash.lang

import io.tashtabash.lang.containers.WordBase
import io.tashtabash.lang.generator.LanguageGenerator
import io.tashtabash.lang.language.Language
import io.tashtabash.lang.language.category.DeixisValue
import io.tashtabash.lang.language.category.InclusivityValue
import io.tashtabash.lang.language.category.NounClassValue.*
import io.tashtabash.lang.language.category.PersonValue.*
import io.tashtabash.lang.language.getClauseAndInfoStr
import io.tashtabash.lang.language.lexis.Word
import io.tashtabash.lang.language.syntax.clause.description.*
import io.tashtabash.lang.language.syntax.context.ActorType
import io.tashtabash.lang.language.syntax.context.Context
import io.tashtabash.lang.language.syntax.context.ContextValue.*
import io.tashtabash.lang.language.syntax.context.ContextValue.Amount.*
import io.tashtabash.lang.language.syntax.context.ContextValue.TimeContext.*
import io.tashtabash.lang.language.syntax.context.ContextValue.TypeContext.*
import io.tashtabash.lang.language.syntax.context.Priority.Explicit
import io.tashtabash.lang.language.syntax.context.Priority.Implicit
import io.tashtabash.random.singleton.RandomSingleton
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
            ActorValue(First, Female, AmountValue(1), DeixisValue.Proximal, null)
        )
        val fourthSubj = PronounDescription(
            "_personal_pronoun",
            listOf(),
            ActorType.Agent,
            ActorValue(First, Female, AmountValue(4), DeixisValue.Proximal, InclusivityValue.Inclusive)
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
            ActorValue(Third, Neutral, AmountValue(10), DeixisValue.ProximalAddressee, null)
        )
        val thirdObj = NominalDescription(
            "hand",
            listOf(),
            ActorComplimentValue(AmountValue(2), DeixisValue.Proximal)
        )
        val fourthObj = NominalDescription(
            "home",
            listOf(),
            ActorComplimentValue(AmountValue(1), null)
        )
        val firstVerb = TransitiveVerbDescription("see", firstSubj, firstObj)
        val secondVerb = TransitiveVerbDescription("see", secondSubj, firstObj)
        val thirdVerb = TransitiveVerbDescription("see", thirdSubj, firstObj)
        val fourthVerb = TransitiveVerbDescription(
            "see",
            thirdSubj,
            secondObj,
            listOf(IndirectObjectDescription(fourthObj, IndirectObjectType.Location))
        )
        val fifthVerb = SimpleIntransitiveVerbDescription("exist", thirdSubj)
        val sixthVerb = TransitiveVerbDescription(
            "build",
            thirdSubj,
            fourthObj,
            listOf(IndirectObjectDescription(thirdObj, IndirectObjectType.Instrument))
        )
        val seventhVerb = TransitiveVerbDescription(
            "build",
            fourthSubj,
            fourthObj,
            listOf(IndirectObjectDescription(secondObj, IndirectObjectType.Instrument))
        )

        val testSentencesMain = listOf(
            TransitiveVerbMainClauseDescription(firstVerb),
            TransitiveVerbMainClauseDescription(secondVerb),
            TransitiveVerbMainClauseDescription(thirdVerb),
            TransitiveVerbMainClauseDescription(fourthVerb),
            IntransitiveVerbMainClauseDescription(fifthVerb),
            TransitiveVerbMainClauseDescription(sixthVerb),
            TransitiveVerbMainClauseDescription(seventhVerb)
        )
        val testSentencesCopula = listOf(
            CopulaMainClauseDescription(CopulaDescription(firstSubj, firstObj)),
            CopulaMainClauseDescription(CopulaDescription(secondSubj, firstObj)),
            CopulaMainClauseDescription(CopulaDescription(thirdSubj, firstObj)),
            PredicatePossessionDescription(thirdSubj, secondObj),
            PredicatePossessionDescription(firstSubj, firstObj)
        )

        val firstContext = Context(
            LongGonePast to Implicit,
            Simple to Explicit
        )
        val secondContext = Context(
            FarFuture to Implicit,
            GeneralQuestion to Explicit
        )
        val thirdContext = Context(
            Regular to Implicit,
            Negative to Explicit
        )

        printSampleClause(testSentencesMain, firstContext, "Main")
        printSampleClause(testSentencesMain, secondContext, "General question")
        printSampleClause(testSentencesMain, thirdContext, "Negative")
        printSampleClause(testSentencesCopula, firstContext, "Copula")
        printSampleClause(testSentencesCopula, secondContext, "Copula question")
        printSampleClause(testSentencesCopula, thirdContext, "Copula negation")
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
        |${
                synonyms.joinToString("\n") { words ->
                    "${words.joinToString { "$it (${it.semanticsCore.connotations})" }} - ${words[0].semanticsCore}"
                }
            }
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


private const val DEFAULT_SEED = 216 + 85
private fun extractSeed(args: Array<String>): Int =
    args.getOrNull(0)
        ?.toIntOrNull()
        ?: run {
            println("No generation seed provided, Using the default seed $DEFAULT_SEED")

            DEFAULT_SEED
        }

/**
 * One parameter expected: a generator seed in the Int range
 */
fun main(args: Array<String>) {
    val seed = extractSeed(args)
    RandomSingleton.safeRandom = Random(seed)

    val generator = LanguageGenerator("SupplementFiles")
    val wordAmount = WordBase("SupplementFiles").baseWords.size
    val language = generator.generateLanguage(wordAmount)

    Visualizer(language).visualize()
}
