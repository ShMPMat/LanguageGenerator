package shmp.lang.language.syntax

import shmp.lang.language.Language
import shmp.lang.language.SpeechPart
import shmp.lang.language.category.Tense
import shmp.lang.language.category.TenseValue
import shmp.lang.language.category.paradigm.ParametrizedCategoryValue
import shmp.lang.language.syntax.context.Context
import shmp.lang.language.syntax.context.ContextValue
import shmp.lang.language.syntax.context.Priority
import kotlin.math.abs


class SyntaxLogic {
    fun resolveVerbForm(
        language: Language,
        context: Context
    ): List<ParametrizedCategoryValue> {
        val (timeValue, priority) = context.time

        val timeValueNumber = timeValue.toNumber()

        if (priority == Priority.Explicit) {
            TODO()
        } else {
            val tense = language.changeParadigm.wordChangeParadigm
                .getSpeechPartParadigm(SpeechPart.Verb)
                .categories
                .filter { it.category is Tense }
                .firstOrNull()
                ?.actualParametrizedValues
                ?.minByOrNull {
                    val tenseNumber = (it.categoryValue as TenseValue).toNumber()

                    abs(tenseNumber - timeValueNumber)
                }
                ?: return emptyList()

            return listOf(tense)
        }
    }

    override fun toString() = ""
}


private fun TenseValue.toNumber() = when(this) {
    TenseValue.Present -> 0
    TenseValue.Future -> 10
    TenseValue.Past -> -100
    TenseValue.DayPast -> -60
    TenseValue.SomeDaysPast -> -70
    TenseValue.MonthPast -> -80
    TenseValue.YearPast -> -90
}

private fun ContextValue.TimeContext.toNumber() = when(this) {
    ContextValue.TimeContext.Present -> 0
    ContextValue.TimeContext.ImmediateFuture -> 7
    ContextValue.TimeContext.ImmediatePast -> -97
    ContextValue.TimeContext.Future -> 10
    ContextValue.TimeContext.FarFuture -> 20
    ContextValue.TimeContext.Past -> -100
    ContextValue.TimeContext.DayPast -> -60
    ContextValue.TimeContext.SomeDaysPast -> -70
    ContextValue.TimeContext.MonthPast -> -80
    ContextValue.TimeContext.YearPast -> -90
    ContextValue.TimeContext.LongGonePast -> -1000
    ContextValue.TimeContext.Regular -> 0
}