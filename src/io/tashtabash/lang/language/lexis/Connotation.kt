package io.tashtabash.lang.language.lexis

import io.tashtabash.lang.language.LanguageException
import kotlin.math.abs


data class Connotation(var name: String, val strength: Double, var isGlobal: Boolean = false) {
    fun getCompatibility(that: Connotation): Double? = (
            if (name == that.name) 1.0
            else connotationsCompatibility[name]?.get(that.name)
            )?.times(strength)

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

class Connotations internal constructor(val _values: LinkedHashMap<Connotation, Connotation>) {
    constructor(values: Set<Connotation>) : this(LinkedHashMap<Connotation, Connotation>().apply {
        values.forEach {
            set(it, it)
        }
    })

    constructor(values: List<Connotation> = listOf()) : this(LinkedHashMap<Connotation, Connotation>().apply {
        values.forEach {
            set(it, it)
        }
    })

    constructor(vararg connotations: Connotation): this(connotations.asList())

    val values = _values.keys

    operator fun plus(that: Connotations): Connotations {
        val newValues = LinkedHashMap<Connotation, Connotation>()
        values.forEach { newValues[it] = it }

        for (value in that.values) {
            newValues[value]?.let {
                val newConnotation = value + it
                newValues[value] = newConnotation
            } ?: run {
                newValues[value] = value
            }
        }

        return Connotations(newValues)
    }

    infix fun distance(that: Connotations): Double {
        if (_values.isEmpty() || that._values.isEmpty())
            return 1.0

        val mutualConnotations = values.mapNotNull { c ->
            val other = that._values[c]
                ?: return@mapNotNull null

            c to other
        }
        val nonMutualElementsNumber = _values.size + that._values.size - 2 * mutualConnotations.size

        return mutualConnotations
            .map { (c1, c2) -> abs(c1.strength - c2.strength) }
            .toMutableList()
            .apply { addAll(List(nonMutualElementsNumber) { 1.0 }) }
            .average()
    }

    infix fun closeness(that: Connotations): Double =
        1.0 - distance(that)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Connotations

        if (values != other.values) return false

        return true
    }

    override fun hashCode(): Int {
        return values.hashCode()
    }

    override fun toString() = if (_values.isNotEmpty())
        values.joinToString()
    else "no connotations"
}


infix fun List<Connotation>.distance(that: List<Connotation>) =
    Connotations(this) distance Connotations(that)

infix fun List<Connotation>.closeness(that: List<Connotation>) =
    1.0 - distance(that)

infix fun Connotations.localDistance(that: Connotations): Double {
    val globalConnotations = values
        .filter { it.isGlobal && that._values[it]?.isGlobal ?: true } +
            that.values.filter { it.isGlobal && _values[it]?.isGlobal ?: true }

    return values
        .filter { it !in globalConnotations } distance that.values.filter { it !in globalConnotations }
}

infix fun Connotations.localCloseness(that: Connotations): Double =
    1.0 - localDistance(that)


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
