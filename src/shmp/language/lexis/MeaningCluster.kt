package shmp.language.lexis

import shmp.language.LanguageException


data class MeaningCluster(val meanings: List<Meaning>) {
    constructor(meaning: Meaning): this(listOf(meaning))

    init {
        if (meanings.isEmpty())
            throw LanguageException("Meaning cluster is empty")
    }

    val size = meanings.size

    operator fun contains(meaning: Meaning) = meanings.contains(meaning)

    operator fun plus(other: MeaningCluster) = MeaningCluster(meanings + other.meanings)

    override fun toString() = meanings.joinToString()
}


// A Double in 0.0..1.0 range, representing
// how similar are two meaning clusters
fun getMeaningDistance(m1: MeaningCluster, m2: MeaningCluster): Double =
    m1.meanings.count { it in m2.meanings } * 2.0 / (m1.meanings.size + m2.meanings.size)

fun getMeaningDistance(m1: MeaningCluster, meaning: Meaning) =
    getMeaningDistance(m1, MeaningCluster(listOf(meaning)))
