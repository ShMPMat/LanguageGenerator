package io.tashtabash.lang

import io.tashtabash.lang.containers.WordBase
import io.tashtabash.lang.generator.LanguageGenerator
import io.tashtabash.lang.language.Language
import io.tashtabash.lang.language.category.DeixisValue
import io.tashtabash.lang.language.category.InclusivityValue
import io.tashtabash.lang.language.category.NounClassValue.*
import io.tashtabash.lang.language.category.PersonValue.*
import io.tashtabash.lang.language.diachronicity.RandomPhonologicalRuleApplicator
import io.tashtabash.lang.language.diachronicity.createDefaultRules
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
        val lightPossessor = NominalDescription(
            "light",
            listOf(),
            ActorComplimentValue(AmountValue(1), null)
        )
        val mothersSubj = NominalDescription(
            "mother",
            listOf(
                AdjectiveDescription("new"),
                PossessorDescription(lightPossessor)
            ),
            ActorComplimentValue(AmountValue(4), DeixisValue.Proximal)
        )
        val fathersSubj = NominalDescription(
            "father",
            listOf(
                AdjectiveDescription("new"),
                AdjectiveDescription("high")
            ),
            ActorComplimentValue(AmountValue(2), DeixisValue.Undefined)
        )
        val iSubj = PronounDescription(
            "_personal_pronoun",
            listOf(),
            ActorType.Agent,
            ActorValue(First, Female, AmountValue(1), DeixisValue.Proximal, null)
        )
        val weSubj = PronounDescription(
            "_personal_pronoun",
            listOf(),
            ActorType.Agent,
            ActorValue(First, Female, AmountValue(4), DeixisValue.Proximal, InclusivityValue.Inclusive)
        )
        val timeObj = NominalDescription(
            "time",
            listOf(AdjectiveDescription("small")),
            ActorComplimentValue(AmountValue(1), DeixisValue.Medial)
        )
        val youPlObj = PronounDescription(
            "_personal_pronoun",
            listOf(),
            ActorType.Agent,
            ActorValue(Second, Neutral, AmountValue(2), DeixisValue.ProximalAddressee, null)
        )
        val theseObj = PronounDescription(
            "_deixis_pronoun",
            listOf(),
            ActorType.Patient,
            ActorValue(Third, Neutral, AmountValue(10), DeixisValue.ProximalAddressee, null)
        )
        val handsObj = NominalDescription(
            "hand",
            listOf(),
            ActorComplimentValue(AmountValue(2), DeixisValue.Proximal)
        )
        val homeObj = NominalDescription(
            "home",
            listOf(),
            ActorComplimentValue(AmountValue(1), null)
        )
        val firstSeeVerb = TransitiveVerbDescription("see", mothersSubj, timeObj)
        val secondSeeVerb = TransitiveVerbDescription("see", fathersSubj, timeObj)
        val thirdSeeVerb = TransitiveVerbDescription("see", iSubj, timeObj)
        val fourthSeeVerb = TransitiveVerbDescription(
            "see",
            iSubj,
            theseObj,
            listOf(IndirectObjectDescription(homeObj, IndirectObjectType.Location))
        )
        val firstHearVerb = TransitiveVerbDescription("hear", iSubj, youPlObj)
        val existVerb = SimpleIntransitiveVerbDescription("exist", iSubj)
        val firstBuildVerb = TransitiveVerbDescription(
            "build",
            iSubj,
            homeObj,
            listOf(IndirectObjectDescription(handsObj, IndirectObjectType.Instrument))
        )
        val secondBuildVerb = TransitiveVerbDescription(
            "build",
            weSubj,
            homeObj,
            listOf(IndirectObjectDescription(theseObj, IndirectObjectType.Instrument))
        )

        val testSentencesMain = listOf(
            TransitiveVerbMainClauseDescription(firstSeeVerb),
            TransitiveVerbMainClauseDescription(secondSeeVerb),
            TransitiveVerbMainClauseDescription(thirdSeeVerb),
            TransitiveVerbMainClauseDescription(fourthSeeVerb),
            TransitiveVerbMainClauseDescription(firstHearVerb),
            IntransitiveVerbMainClauseDescription(existVerb),
            TransitiveVerbMainClauseDescription(firstBuildVerb),
            TransitiveVerbMainClauseDescription(secondBuildVerb)
        )
        val testSentencesCopula = listOf(
            CopulaMainClauseDescription(CopulaDescription(mothersSubj, timeObj)),
            CopulaMainClauseDescription(CopulaDescription(fathersSubj, timeObj)),
            CopulaMainClauseDescription(CopulaDescription(iSubj, timeObj)),
            PredicatePossessionDescription(iSubj, theseObj),
            PredicatePossessionDescription(mothersSubj, timeObj)
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

        for (it in clauses) {
            printSampleClause(it, context)
            println("=")
        }

        println()
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

private const val DEFAULT_SEED = 216 + 48
private fun extractSeed(args: Array<String>): Int =
    args.getOrNull(0)
        ?.toIntOrNull()
        ?: run {
            println("No generation seed provided, Using the default seed $DEFAULT_SEED")

            DEFAULT_SEED
        }

val CHANGES_NUMBER = 5

/**
 * One parameter expected: a generator seed in the Int range
 */
fun main(args: Array<String>) {
    val seed = extractSeed(args)
    RandomSingleton.safeRandom = Random(seed)

    val generator = LanguageGenerator("SupplementFiles")
    val wordAmount = WordBase("SupplementFiles").baseWords.size
    var language = generator.generateLanguage(wordAmount)

    val phonologicalRulesContainer = createDefaultRules(generator.phonemePool)
    for (i in 0 until CHANGES_NUMBER) {
        val ruleApplicator = RandomPhonologicalRuleApplicator()
        language = ruleApplicator.applyRandomPhonologicalRule(language, phonologicalRulesContainer)
        println(ruleApplicator.messages.joinToString("\n"))
    }

    Visualizer(language).visualize()
}
