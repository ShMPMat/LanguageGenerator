package io.tashtabash.lang.language.syntax.context

import io.tashtabash.lang.language.category.*


sealed class ContextValue {
    sealed class TimeContext : ContextValue() {
        object Present : TimeContext()
        object ImmediateFuture : TimeContext()
        object ImmediatePast : TimeContext()

        object Future : TimeContext()
        object FarFuture : TimeContext()

        object Past : TimeContext()
        object DayPast : TimeContext()
        object SomeDaysPast : TimeContext()
        object MonthPast : TimeContext()
        object YearPast : TimeContext()
        object LongGonePast : TimeContext()

        object Regular: TimeContext()
    }

    sealed class TypeContext : ContextValue() {
        object Simple: TypeContext()
        object GeneralQuestion: TypeContext()
        object Negative: TypeContext()
    }

    data class ActorComplimentValue(val number: Amount, val deixis: DeixisValue? = null)

    data class ActorValue(
        val person: PersonValue,
        val nounClass: NounClassValue,
        val number: Amount,
        val deixis: DeixisValue,
        val inclusive: InclusivityValue?
    ) : ContextValue()

    sealed class Amount: ContextValue()  {
        data class AmountValue(val amount: Int): Amount()
        object All: Amount()
    }

    override fun toString() = this::class.simpleName ?: ""
}


enum class ActorType {
    Agent,
    Patient
}

typealias ContextValues = Set<ContextValue>
