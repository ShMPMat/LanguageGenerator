package io.tashtabash.lang

import io.tashtabash.lang.containers.WordBase
import io.tashtabash.lang.generator.LanguageGenerator
import io.tashtabash.lang.language.Language
import io.tashtabash.lang.language.analyzer.getIdenticalWordFormFraction
import io.tashtabash.lang.language.analyzer.getIdenticalWordForms
import io.tashtabash.lang.language.category.DeixisValue
import io.tashtabash.lang.language.category.InclusivityValue
import io.tashtabash.lang.language.category.NounClassValue.*
import io.tashtabash.lang.language.category.PersonValue.*
import io.tashtabash.lang.language.diachronicity.TendencyBasedPhonologicalRuleApplicator
import io.tashtabash.lang.language.diachronicity.createDefaultRules
import io.tashtabash.lang.language.getClauseAndInfoStr
import io.tashtabash.lang.language.syntax.clause.description.*
import io.tashtabash.lang.language.syntax.context.Context
import io.tashtabash.lang.language.syntax.context.ContextValue.*
import io.tashtabash.lang.language.syntax.context.ContextValue.Amount.*
import io.tashtabash.lang.language.syntax.context.ContextValue.TimeContext.*
import io.tashtabash.lang.language.syntax.context.ContextValue.TypeContext.*
import io.tashtabash.lang.language.syntax.context.Priority.Explicit
import io.tashtabash.lang.language.syntax.context.Priority.Implicit
import io.tashtabash.lang.language.syntax.sequence.WordSequence
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
            ActorComplimentValue(AmountValue(1), null),
        )
        val mothers = NominalDescription(
            "mother",
            ActorComplimentValue(AmountValue(4), DeixisValue.Proximal),
            listOf(
                AdjectiveDescription("new"),
                PossessorDescription(lightPossessor)
            )
        )
        val fathers = NominalDescription(
            "father",
            ActorComplimentValue(AmountValue(2), DeixisValue.Undefined),
            listOf(
                AdjectiveDescription("new"),
                AdjectiveDescription("high")
            )
        )
        val i = PronounDescription(
            "_personal_pronoun",
            ActorValue(First, Female, AmountValue(1), DeixisValue.Proximal, null),
        )
        val we = PronounDescription(
            "_personal_pronoun",
            ActorValue(First, Female, AmountValue(4), DeixisValue.Proximal, InclusivityValue.Inclusive),
        )
        val time = NominalDescription(
            "time",
            ActorComplimentValue(AmountValue(1), DeixisValue.Medial),
            listOf(AdjectiveDescription("small"))
        )
        val youPl = PronounDescription(
            "_personal_pronoun",
            ActorValue(Second, Neutral, AmountValue(2), DeixisValue.ProximalAddressee, null),
        )
        val these = PronounDescription(
            "_deixis_pronoun",
            ActorValue(Third, Neutral, AmountValue(10), DeixisValue.ProximalAddressee, null),
        )
        val handsObj = NominalDescription(
            "hand",
            ActorComplimentValue(AmountValue(2), DeixisValue.Proximal),
        )
        val home = NominalDescription(
            "home",
            ActorComplimentValue(AmountValue(1), null),
        )
        val firstSeeVerb = TransitiveVerbDescription("see", mothers, time)
        val secondSeeVerb = TransitiveVerbDescription("see", fathers, time)
        val thirdSeeVerb = TransitiveVerbDescription("see", i, time)
        val fourthSeeVerb = TransitiveVerbDescription(
            "see",
            i,
            these,
            listOf(IndirectObjectDescription(home, IndirectObjectType.Location))
        )
        val firstHearVerb = TransitiveVerbDescription("hear", i, youPl)
        val existVerb = IntransitiveVerbDescription("exist", i)
        val firstBuildVerb = TransitiveVerbDescription(
            "build",
            i,
            home,
            listOf(IndirectObjectDescription(handsObj, IndirectObjectType.Instrument))
        )
        val secondBuildVerb = TransitiveVerbDescription(
            "build",
            we,
            home,
            listOf(
                IndirectObjectDescription(these, IndirectObjectType.Instrument),
                IndirectObjectDescription(youPl, IndirectObjectType.Benefactor)
            )
        )
        val nounBeneficiaryBuildVerb = TransitiveVerbDescription(
            "build",
            we,
            home,
            listOf(
                IndirectObjectDescription(these, IndirectObjectType.Instrument),
                IndirectObjectDescription(fathers, IndirectObjectType.Benefactor)
            )
        )

        val testSentencesMain = listOf(
            TransitiveVerbMainClauseDescription(firstSeeVerb),
            TransitiveVerbMainClauseDescription(secondSeeVerb),
            TransitiveVerbMainClauseDescription(thirdSeeVerb),
            TransitiveVerbMainClauseDescription(fourthSeeVerb),
            TransitiveVerbMainClauseDescription(firstHearVerb),
            IntransitiveVerbMainClauseDescription(existVerb),
            TransitiveVerbMainClauseDescription(firstBuildVerb),
            TransitiveVerbMainClauseDescription(secondBuildVerb),
            TransitiveVerbMainClauseDescription(nounBeneficiaryBuildVerb),
        )
        val testSentencesCopula = listOf(
            CopulaMainClauseDescription(CopulaDescription(mothers, time)),
            CopulaMainClauseDescription(CopulaDescription(fathers, time)),
            CopulaMainClauseDescription(CopulaDescription(i, time)),
            PredicatePossessionDescription(i, these),
            PredicatePossessionDescription(mothers, time)
        )

        val firstContext = Context(
            LongGonePast to Implicit,
            Indicative to Explicit
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
        val wordSequence = clause.toClause(language, context, Random(10))
            .unfold(language, Random(10))

        println(getClauseAndInfoStr(wordSequence))
    }

    private fun printAdditionalLexisInfo() {
        val synonyms = language.lexis.words
            .groupBy { it.semanticsCore.toString() }
            .map { it.value }
            .filter { it.size > 1 }
        val homophoneStats = getIdenticalWordFormFraction(language)

        print(
            """
        |Synonyms:
        |${
                synonyms.joinToString("\n") { words ->
                    "${words.joinToString { "${it.getPhoneticRepresentation()} (${it.semanticsCore.connotations})" }} - ${words[0].semanticsCore}"
                }
            }
        |
        |Lexis size - ${language.lexis.size} words
        |
        |Derivations:
        |${
                language.lexis.words
                    .filter { it.semanticsCore.changeHistory != null }
                    .sortedBy { it.semanticsCore.computeChangeDepth(language.lexis) }
                    .joinToString("\n\n") { language.lexis.computeHistory(it) }
            }
        |
        |Collapsed meanings:
        |${
                language.lexis.words
                    .filter { it.semanticsCore.meaningCluster.size > 1 }
                    .joinToString("\n") {
                        "${it.getPhoneticRepresentation()} - ${it.semanticsCore.meaningCluster}"
                    }
            }
        |
        |Identical forms for all words (%.2f): ${homophoneStats.homophoneFormsCount}/${homophoneStats.allWordFormsCount}
        |${
                getIdenticalWordForms(language)
                    .sortedBy { -it.size }
                    .joinToString("\n\n") { words ->
                        val joinedWords = words.map { it.first }
                            .reduce(WordSequence::plus)
                        getClauseAndInfoStr(joinedWords)
                    }
            }
    """.trimMargin().format(homophoneStats.homophoneFraction)
        )
    }
}

private const val DEFAULT_SEED = 216
private fun extractSeed(args: Array<String>): Int =
    args.getOrNull(0)
        ?.toIntOrNull()
        ?: run {
            println("No generation seed provided, Using the default seed $DEFAULT_SEED")

            DEFAULT_SEED
        }

val CHANGES_NUMBER = 10
val visualizeBeforeChanges = false

/**
 * One parameter expected: a generator seed in the Int range
 */
fun main(args: Array<String>) {
    val seed = extractSeed(args)
    RandomSingleton.safeRandom = Random(seed)

    val generator = LanguageGenerator("SupplementFiles")
    val wordAmount = WordBase("SupplementFiles").baseWords.size
    var language = generator.generateLanguage(wordAmount)

    if (visualizeBeforeChanges) {
        Visualizer(language).visualize()
        println("\n\n\n\n\n\n\n\n\n\n\n\n\n")
    }

    val phonologicalRulesContainer = createDefaultRules(generator.phonemeGenerator.allPossiblePhonemes)
    val ruleApplicator = TendencyBasedPhonologicalRuleApplicator(generator.phonemeGenerator.allPossiblePhonemes)
    var messagesSize = 0
    for (i in 0 until CHANGES_NUMBER) {
        language = ruleApplicator.applyPhonologicalRule(language, phonologicalRulesContainer)
        println(ruleApplicator.messages.drop(messagesSize).joinToString("\n"))
        messagesSize = ruleApplicator.messages.size
    }
    print("\n\n")

    Visualizer(language).visualize()
}
