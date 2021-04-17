package shmp.lang.language.lexis

import shmp.lang.language.LanguageException


data class Connotation(val name: String, val strength: Double) {
    fun getCompatibility(that: Connotation) = (if (name == that.name) 1.0
    else connotationsCompatibility[name]?.get(that.name))?.times(strength)

    operator fun plus(that: Connotation): Connotation {
        if (name != that.name)
            throw LanguageException("Cannot sum connotations $name and ${that.name}")

        return Connotation(name, strength + that.strength)
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

data class Connotations(val values: Set<Connotation>) {
    operator fun plus(that: Connotations): Connotations {
        val values = values.toMutableSet()

        for (value in that.values) {
            if (value in values) {
                val newConnotation = value + values.first { it == value }
                values.remove(value)
                values.add(newConnotation)
            } else
                values.add(value)
        }

        return Connotations(values)
    }

    override fun toString() = if (values.isNotEmpty())
        values.joinToString()
    else "no connotations"
}


val connotationsCompatibility = mapOf<String, Map<String, Double>>(
    "small" to mapOf("big" to 0.0),
    "big" to mapOf("small" to 0.0),
    "young" to mapOf("old" to 0.0),
    "old" to mapOf("young" to 0.0)
)
