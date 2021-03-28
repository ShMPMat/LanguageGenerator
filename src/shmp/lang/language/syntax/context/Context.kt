package shmp.lang.language.syntax.context

import shmp.lang.language.syntax.context.ContextValue.*


data class Context(
    val time: PrioritizedValue<TimeContext>,
    val type: PrioritizedValue<TypeContext>,
    val actors: MutableMap<ActorType, ActorValue> = mutableMapOf()
)

typealias PrioritizedValue<E> = Pair<E, Priority>

enum class Priority {
    Implicit,
    Explicit
}
