package shmp.lang.language.syntax

import shmp.lang.language.*
import shmp.lang.language.category.*
import shmp.lang.language.category.paradigm.SourcedCategory
import shmp.lang.language.category.paradigm.SourcedCategoryValues
import shmp.lang.language.lexis.TypedSpeechPart
import shmp.lang.language.syntax.context.ActorType
import shmp.lang.language.syntax.context.Context
import shmp.lang.language.syntax.context.ContextValue.*
import shmp.lang.language.syntax.context.Priority
import shmp.lang.language.syntax.features.CopulaType
import kotlin.math.abs


class SyntaxLogic(
    private val verbFormSolver: Map<VerbContextInfo, SourcedCategoryValues>,
    private val verbCasesSolver: Map<Pair<Pair<TypedSpeechPart, Set<CategoryValue>>, SyntaxRelation>, CategoryValues>,
    private val copulaCaseSolver: Map<Pair<Pair<CopulaType, SyntaxRelation>, TypedSpeechPart>, CategoryValues>,
    private val nonCoreCaseSolver: Map<Pair<CaseValue, TypedSpeechPart>, CategoryValues>,
    private val numberCategorySolver: NumberCategorySolver?,
    private val nounClassCategorySolver: Map<NounClassValue, NounClassValue>?,
    private val deixisCategorySolver: Map<Pair<DeixisValue?, TypedSpeechPart>, CategoryValues>,
    private val personalPronounDropSolver: PersonalPronounDropSolver,
    private val personalPronounInclusivity: SourcedCategory? // WALS only knows about separate inclusive
) {
    val defaultInclusivity = if (personalPronounInclusivity != null) InclusivityValue.Exclusive else null

    fun resolvePronounCategories(actorValue: ActorValue, speechPart: TypedSpeechPart): CategoryValues {
        val resultCategories = mutableListOf<CategoryValue>()
        val (person, gender, amount, deixis, inclusivity) =
            actorValue

        resultCategories.add(person)
        resultCategories.addAll(deixisCategorySolver.getOrDefault(deixis to speechPart, listOf()))
        resultCategories.addNumber(amount)

        if (nounClassCategorySolver != null)
            resultCategories.add(nounClassCategorySolver.getValue(gender))

        inclusivity?.let { resultCategories.add(it) } ?: run {
            if (personalPronounInclusivity?.compulsoryData?.isApplicable(resultCategories) == true) {
                resultCategories.add(InclusivityValue.Exclusive)
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

        resultCategories.addAll(deixisCategorySolver.getOrDefault(deixis to speechPart, listOf()))
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
        resolveVerbTime(language, verbType, context)

    private fun resolveVerbTime(language: Language, verbType: TypedSpeechPart, context: Context): SourcedCategoryValues {
        val (timeValue, priority) = context.time

        verbFormSolver[verbType to timeValue]?.let { categories ->
            return categories.map { it }
        }

        if (priority == Priority.Explicit) {
            TODO()
        } else
            return chooseClosestTense(language, verbType, timeValue)
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
        verbFormSolver.entries.map { (context, categories) ->
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
        deixisCategorySolver.entries.map { (g1, g2) ->
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