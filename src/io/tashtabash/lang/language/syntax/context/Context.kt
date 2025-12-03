package io.tashtabash.lang.language.syntax.context

import io.tashtabash.lang.language.syntax.context.ContextValue.*


data class Context(
    val time: PrioritizedValue<TimeContext>,
    val type: PrioritizedValue<TypeContext>
)

typealias PrioritizedValue<E> = Pair<E, Priority>

enum class Priority {
    Implicit,
    Explicit
}
