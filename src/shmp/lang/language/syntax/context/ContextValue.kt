package shmp.lang.language.syntax.context

import shmp.lang.language.category.DeixisValue
import shmp.lang.language.category.NounClassValue
import shmp.lang.language.category.PersonValue


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
    }

    data class ActorComplimentValue(val number: AmountValue, val deixis: DeixisValue?)

    data class ActorValue(
        val person: PersonValue,
        val nounClass: NounClassValue,
        val number: AmountValue,
        val deixis: DeixisValue
    ) : ContextValue()

    data class AmountValue(val amount: Int): ContextValue()

    override fun toString() = this::class.simpleName ?: ""
}


enum class ActorType {
    Agent,
    Patient
}

typealias ContextValues = Set<ContextValue>