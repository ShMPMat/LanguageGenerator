package shmp.lang.language.syntax

import shmp.lang.language.*
import shmp.lang.language.category.*
import shmp.lang.language.category.paradigm.ParametrizedCategoryValues
import shmp.lang.language.category.paradigm.parametrize
import shmp.lang.language.syntax.context.ActorType
import shmp.lang.language.syntax.context.Context
import shmp.lang.language.syntax.context.ContextValue
import shmp.lang.language.syntax.context.ContextValue.TimeContext
import shmp.lang.language.syntax.context.Priority
import kotlin.math.abs


class SyntaxLogic(
    val verbFormSolver: Map<TimeContext, CategoryValues>,
    val numberCategorySolver: Map<NumbersValue, IntRange>?,
    val genderCategorySolver: Map<GenderValue, GenderValue>?,
    val deixisCategorySolver: Map<DeixisValue?, CategoryValues>,
    private val personalPronounDropSolver: PersonalPronounDropSolver
) {
    fun resolvePronounCategories(actorValue: ContextValue.ActorValue): CategoryValues {
        val resultCategories = mutableListOf<CategoryValue>()
        val (person, gender, amount, deixis) = actorValue

        resultCategories.add(person)
        resultCategories.addAll(deixisCategorySolver.getValue(deixis))
        resultCategories.addNumber(amount)

        if (genderCategorySolver != null)
            resultCategories.add(genderCategorySolver.getValue(gender))

        return resultCategories
    }

    fun resolveComplimentCategories(actorCompliment: ContextValue.ActorComplimentValue): CategoryValues {
        val resultCategories = mutableListOf<CategoryValue>()
        val (amount, deixis) = actorCompliment

        resultCategories.addAll(deixisCategorySolver.getValue(deixis))
        resultCategories.addNumber(amount)

        return resultCategories
    }

    private fun MutableList<CategoryValue>.addNumber(amount: ContextValue.AmountValue) {
        if (numberCategorySolver != null) {
            val number = numberCategorySolver.entries
                .firstOrNull { it.value.contains(amount.amount) }
                ?.key
                ?: throw LanguageException("No handler for amount ${amount.amount}")

            add(number)
        }
    }

    fun resolvePersonalPronounDrop(categories: List<CategoryValue>, actorType: ActorType): Boolean =
        personalPronounDropSolver
            .any { (a, cs) ->
                a == actorType
                        && categories.all { it in cs }
                        && cs.size == categories.size
            }


    fun resolveVerbForm(
        language: Language,
        context: Context
    ): ParametrizedCategoryValues {
        val (timeValue, priority) = context.time

        verbFormSolver[timeValue]?.let { categories ->
            return categories.map { it.parametrize(CategorySource.SelfStated) }
        }

        if (priority == Priority.Explicit) {
            TODO()
        } else
            return chooseClosestTense(language, timeValue)
    }

    private fun chooseClosestTense(language: Language, timeContext: TimeContext): ParametrizedCategoryValues {
        val timeValueNumber = timeContext.toNumber()

        val tense = language.changeParadigm.wordChangeParadigm
            .getSpeechPartParadigm(SpeechPart.Verb)
            .categories
            .firstOrNull { it.category is Tense }
            ?.actualParametrizedValues
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
        verbFormSolver.entries.joinToString("\n") { (context, categoties) ->
            "For $context the following form is used: " + categoties.joinToString(", ")
        }
    }
        |
        |${
        numberCategorySolver?.entries?.joinToString("\n") { (number, range) ->
            "$number is used for amounts $range"
        } ?: ""
    } 
        |
        |${
        genderCategorySolver?.entries?.joinToString("\n") { (g1, g2) ->
            "$g1 is seen as $g2"
        } ?: ""
    } 
        |${
        deixisCategorySolver.entries.joinToString("\n") { (g1, g2) ->
            "$g1 is expressed as $g2"
        } ?: ""
    } 
        |
        |Dropped pronouns:
        |${
        personalPronounDropSolver.joinToString("\n") { (g1, g2) ->
            "$g1 with categories ${g2.joinToString(".") { it.shortName }}"
        } + if (personalPronounDropSolver.isEmpty()) "none" else ""
    } 
        |
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

typealias PersonalPronounDropSolver = List<Pair<ActorType, CategoryValues>>