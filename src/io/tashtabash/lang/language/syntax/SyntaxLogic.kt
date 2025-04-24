package io.tashtabash.lang.language.syntax

import io.tashtabash.lang.language.*
import io.tashtabash.lang.language.category.*
import io.tashtabash.lang.language.category.paradigm.SourcedCategory
import io.tashtabash.lang.language.category.paradigm.SourcedCategoryValues
import io.tashtabash.lang.language.category.value.CategoryValue
import io.tashtabash.lang.language.category.value.CategoryValues
import io.tashtabash.lang.language.lexis.TypedSpeechPart
import io.tashtabash.lang.language.syntax.context.ActorType
import io.tashtabash.lang.language.syntax.context.Context
import io.tashtabash.lang.language.syntax.context.ContextValue.*
import io.tashtabash.lang.language.syntax.context.Priority
import io.tashtabash.lang.language.syntax.features.CopulaType
import kotlin.math.abs


class SyntaxLogic(
    private val timeFormSolver: Map<VerbContextInfo, SourcedCategoryValues> = mapOf(),
    private val verbCasesSolver: Map<Pair<Pair<TypedSpeechPart, Set<CategoryValue>>, SyntaxRelation>, CategoryValues> = mapOf(),
    private val copulaCaseSolver: Map<Pair<Pair<CopulaType, SyntaxRelation>, TypedSpeechPart>, CategoryValues> = mapOf(),
    private val nonCoreCaseSolver: Map<Pair<CaseValue, TypedSpeechPart>, CategoryValues> = mapOf(),
    private val numberCategorySolver: NumberCategorySolver? = null,
    private val nounClassCategorySolver: Map<NounClassValue, NounClassValue>? = mapOf(),
    private val deixisDefinitenessCategorySolver: Map<Pair<DeixisValue?, TypedSpeechPart>, CategoryValues> = mapOf(),
    private val personalPronounDropSolver: PersonalPronounDropSolver = listOf(),
    private val personalPronounInclusivity: SourcedCategory? = null // WALS only knows about separate inclusive
) {
    val defaultInclusivity = if (personalPronounInclusivity != null) InclusivityValue.Exclusive else null

    fun resolvePronounCategories(actorValue: ActorValue, speechPart: TypedSpeechPart): CategoryValues {
        val resultCategories = mutableListOf<CategoryValue>()
        val (person, gender, amount, deixis, inclusivity) =
            actorValue

        resultCategories += person
        resultCategories += deixisDefinitenessCategorySolver.getOrDefault(deixis to speechPart, listOf())
        resultCategories.addNumber(amount)

        if (nounClassCategorySolver != null)
            resultCategories += nounClassCategorySolver.getValue(gender)

        inclusivity?.let { resultCategories += it } ?: run {
            if (personalPronounInclusivity?.compulsoryData?.isApplicable(resultCategories) == true) {
                resultCategories += InclusivityValue.Exclusive
            }
        }

        return resultCategories
    }

    fun resolveComplimentCategories(
        actorCompliment: ActorComplimentValue,
        speechPart: TypedSpeechPart
    ): CategoryValues {
        val resultCategories = mutableListOf<CategoryValue>()
        val (amount, deixis) = actorCompliment

        resultCategories += deixisDefinitenessCategorySolver.getOrDefault(deixis to speechPart, listOf())
        resultCategories.addNumber(amount)

        return resultCategories
    }

    private fun MutableList<CategoryValue>.addNumber(amount: Amount) {
        if (numberCategorySolver != null) {
            val number = when (amount) {
                is Amount.All -> numberCategorySolver.allForm
                is Amount.AmountValue -> numberCategorySolver.amountMap.entries
                    .firstOrNull { it.value.contains(amount.amount) }
                    ?.key
                    ?: throw LanguageException("No handler for amount ${amount.amount}")
            }
            add(number)
        }
    }

    fun resolvePersonalPronounDrop(categories: CategoryValues, actorType: ActorType): Boolean =
        personalPronounDropSolver
            .any { (a, cs) ->
                a == actorType
                        && categories.all { it in cs }
                        && cs.size == categories.size
            }

    fun resolveVerbCase(
        verbType: TypedSpeechPart,
        syntaxRelation: SyntaxRelation,
        categories: Set<CategoryValue>
    ): CategoryValues {
        return verbCasesSolver.getValue(verbType to categories to syntaxRelation)
    }

    fun resolveCopulaCase(
        copulaType: CopulaType,
        syntaxRelation: SyntaxRelation,
        speechPart: TypedSpeechPart
    ): CategoryValues {
        return copulaCaseSolver.getValue(copulaType to syntaxRelation to speechPart)
    }

    fun resolveNonCoreCase(caseValue: CaseValue, speechPart: TypedSpeechPart): CategoryValues {
        if (caseValue !in nonCoreCases)
            throw SyntaxException("Cannot resolve core cases")

        return nonCoreCaseSolver.getValue(caseValue to speechPart)
    }

    fun resolveVerbForm(language: Language, verbType: TypedSpeechPart, context: Context) =
        resolveTime(language, verbType, context)

    fun resolveAdjectiveForm(language: Language, adjectiveType: TypedSpeechPart, context: Context) =
        resolveTime(language, adjectiveType, context)

    private fun resolveTime(language: Language, speechPart: TypedSpeechPart, context: Context): SourcedCategoryValues {
        val (timeValue, priority) = context.time

        timeFormSolver[speechPart to timeValue]?.let { categories ->
            return categories.map { it }
        }

        if (priority == Priority.Explicit) {
            TODO()
        } else
            return chooseClosestTense(language, speechPart, timeValue)
    }

    private fun chooseClosestTense(
        language: Language,
        verbType: TypedSpeechPart,
        timeContext: TimeContext
    ): SourcedCategoryValues {
        val timeValueNumber = timeContext.toNumber()

        val tense = language.changeParadigm.wordChangeParadigm
            .getSpeechPartParadigm(verbType)
            .categories
            .firstOrNull { it.category is Tense }
            ?.actualSourcedValues
            ?.minByOrNull {
                val tenseNumber = (it.categoryValue as TenseValue).toNumber()

                abs(tenseNumber - timeValueNumber)
            }
            ?: return emptyList()

        return listOf(tense)
    }

    override fun toString() = """
        |Syntax:
        |
        |${
        timeFormSolver.entries.map { (context, categories) ->
            listOf(
                "For ${context.first}, ",
                "${context.second} ",
                " the following form is used: ",
                categories.joinToString(", ")
            )
        }
            .lineUpAll()
            .joinToString("\n")
    }
        |
        |${
        verbCasesSolver.entries.map { (context, categories) ->
            listOf(
                "For ${context.first.first}, ",
                "${context.first.second}, ",
                "${context.second} ",
                " the following cases are used: ",
                categories.joinToString(", ")
            )
        }
            .lineUpAll()
            .joinToString("\n")
    }
        |
        |${
        numberCategorySolver?.amountMap?.entries?.map { (number, range) ->
            listOf("$number ", "is used for amounts $range")
        }
            ?.lineUpAll()
            ?.joinToString("\n")
            ?: ""
    }
        |All instances - ${numberCategorySolver?.allForm}
        |
        |${
        nounClassCategorySolver?.entries?.map { (g1, g2) ->
            listOf("$g1", " is seen as $g2")
        }
            ?.lineUpAll()
            ?.joinToString("\n")
            ?: ""
    }
        | 
        |${
        deixisDefinitenessCategorySolver.entries.map { (g1, g2) ->
            listOf("${g1.first}, ", "${g1.second} ", " is expressed as $g2")
        }
            .lineUpAll()
            .joinToString("\n")
    }
        |
        |Dropped pronouns:
        |
        |${
        personalPronounDropSolver.map { (g1, g2) ->
            listOf("$g1", " with categories ${g2.joinToString(".") { it.alias }}")
        }
            .lineUpAll()
            .joinToString("\n") + if (personalPronounDropSolver.isEmpty()) "none" else ""
    }
        |
        |${
        copulaCaseSolver.entries.map { (context, categories) ->
            listOf(
                "For ${context.first.first}, ",
                "${context.first.second}, ",
                "${context.second} ",
                " the following cases are used: " + categories.joinToString(", ")
            )
        }
            .lineUpAll()
            .joinToString("\n")
    }
        |
        |${
        nonCoreCaseSolver.entries.map { (context, categories) ->
            listOf(
                "For ${context.first}, ",
                "${context.second} ",
                " the following cases are used: " + categories.joinToString(", ")
            )
        }
            .lineUpAll()
            .joinToString("\n")
    }
    """.trimMargin()
}


private fun TenseValue.toNumber() = when (this) {
    TenseValue.Present -> 0
    TenseValue.Future -> 10
    TenseValue.Past -> -100
    TenseValue.DayPast -> -60
    TenseValue.SomeDaysPast -> -70
    TenseValue.MonthPast -> -80
    TenseValue.YearPast -> -90
}

private fun TimeContext.toNumber() = when (this) {
    TimeContext.Present -> 0.0
    TimeContext.ImmediateFuture -> 7.0
    TimeContext.ImmediatePast -> -97.0
    TimeContext.Future -> 10.0
    TimeContext.FarFuture -> 20.0
    TimeContext.Past -> -100.0
    TimeContext.DayPast -> -60.0
    TimeContext.SomeDaysPast -> -70.0
    TimeContext.MonthPast -> -80.0
    TimeContext.YearPast -> -90.0
    TimeContext.LongGonePast -> -1000.0
    TimeContext.Regular -> Double.NaN
}

data class NumberCategorySolver(val amountMap: Map<NumberValue, IntRange>, val allForm: NumberValue)

typealias PersonalPronounDropSolver = List<Pair<ActorType, CategoryValues>>

typealias VerbContextInfo = Pair<TypedSpeechPart, TimeContext>