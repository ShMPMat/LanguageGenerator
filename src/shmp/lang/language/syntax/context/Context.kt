package shmp.lang.language.syntax.context

import shmp.lang.language.syntax.context.ContextValue.*


data class Context(val time: PrioritizedValue<TimeContext>) {

}

typealias PrioritizedValue<E> = Pair<E, Priority>

enum class Priority {
    Implicit,
    Explicit
}
