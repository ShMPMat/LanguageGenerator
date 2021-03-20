package shmp.lang.language.syntax.context


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
}


typealias ContextValues = Set<ContextValue>