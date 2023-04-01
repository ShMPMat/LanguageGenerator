package io.tashtabash.lang.language.lexis

import io.tashtabash.lang.language.LanguageException


data class Connotation(var name: String, val strength: Double, var isGlobal: Boolean = false) {
    fun getCompatibility(that: Connotation) = (if (name == that.name) 1.0
    else connotationsCompatibility[name]?.get(that.name))?.times(strength)

    operator fun plus(that: Connotation): Connotation {
        if (name != that.name)
            throw LanguageException("Cannot sum connotations $name and ${that.name}")

        return Connotation(name, strength + that.strength, isGlobal || that.isGlobal)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Connotation) return false

        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

    override fun toString() = "$name:$strength"
}

data class Connotations(val values: Set<Connotation> = setOf()) {
    constructor(values: List<Connotation>): this(values.toSet())

    operator fun plus(that: Connotations): Connotations {
        val values = values.toMutableSet()

        for (value in that.values) {
            if (value in values) {
                val newConnotation = value + values.first { it == value }
                values.remove(value)
                values += newConnotation
            } else
                values += value
        }

        return Connotations(values)
    }

    override fun toString() = if (values.isNotEmpty())
        values.joinToString()
    else "no connotations"
}

infix fun Connotations.distance(that: Connotations): Double {
    val mutualConnotations = values.mapNotNull { c ->
        val other = that.values.firstOrNull { c == it }
            ?: return@mapNotNull null

        c to other
    }
    val otherElementsAmount = values.size + that.values.size - 2 * mutualConnotations.size

    if (values.isEmpty() || that.values.isEmpty())
        return 0.0

    return mutualConnotations
        .map { (c1, c2) -> c1.strength * c2.strength }
        .toMutableList()
        .apply { addAll(List(otherElementsAmount) { 0.0 }) }
        .average()
}

infix fun List<Connotation>.distance(that: List<Connotation>) =
    Connotations(this) distance Connotations(that)

infix fun Connotations.localDistance(that: Connotations): Double {
    val globalConnotations = values
        .filter { it.isGlobal && that.values.firstOrNull { c -> c == it }?.isGlobal ?: true } +
            that.values.filter { it.isGlobal && values.firstOrNull { c -> c == it }?.isGlobal ?: true }

    return values.filter { it !in globalConnotations } distance that.values.filter { it !in globalConnotations }
}


val connotationsCompatibility = mapOf<String, Map<String, Double>>(
    "small" to mapOf("big" to 0.0),
    "big" to mapOf("small" to 0.0),
    "young" to mapOf("old" to 0.0),
    "old" to mapOf("young" to 0.0),
    "comfort" to mapOf("not_comfort" to 0.0),
    "not_comfort" to mapOf("comfort" to 0.0),
    "defence" to mapOf("danger" to 0.0),
    "danger" to mapOf("defence" to 0.0),
    "light" to mapOf("darkness" to 0.0),
    "darkness" to mapOf("light" to 0.0),
    "understand" to mapOf("not_understand" to 0.0),
    "not_understand" to mapOf("understand" to 0.0)
)
