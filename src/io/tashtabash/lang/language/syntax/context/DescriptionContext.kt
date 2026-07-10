package io.tashtabash.lang.language.syntax.context

import io.tashtabash.lang.language.syntax.clause.description.ObjectType
import io.tashtabash.lang.language.syntax.context.ContextValue.*


data class DescriptionContext(
    val time: PrioritizedValue<TimeContext>,
    val type: List<PrioritizedValue<TypeContext>> = listOf(), // No values means the boring indicative clause
    val topic: ObjectType? = null
)

typealias PrioritizedValue<E> = Pair<E, Priority>

enum class Priority {
    Implicit,
    Explicit
}
