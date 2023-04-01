package io.tashtabash.lang.generator.util

import io.tashtabash.lang.language.lexis.Meaning
import io.tashtabash.random.UnwrappableSSO
import io.tashtabash.random.testProbability
import java.io.File
import kotlin.random.Random


data class WordClusterContainer(val clusters: List<WordCluster>)

data class WordCluster(val meanings: List<WordClusterInstance>) {
    init {
        if (meanings.none { it.probability == 1.0 })
            throw DataConsistencyException("No main word in the WordCluster $this")
    }

    val main = meanings.first { it.probability == 1.0 }.meaning

    fun chooseMeanings(random: Random) = meanings.mapNotNull { (m, p) ->
        if (testProbability(p, random))
            m
        else null
    }
}

data class WordClusterInstance(val meaning: Meaning, override val probability: Double): UnwrappableSSO<Meaning>(meaning)

fun readWordClusters(supplementPath: String): WordClusterContainer {
    val clusters = File("$supplementPath/WordClusters")
        .readLines()
        .filter { it.isNotBlank() }
        .map {
            val cluster = it.drop(1).dropLast(1).split(",").map { t -> readWordClusterInstance(t) }
            WordCluster(cluster)
        }

    return WordClusterContainer(clusters)
}

private fun readWordClusterInstance(tag: String): WordClusterInstance {
    val (name, prob) = tag.split(":")
    return WordClusterInstance(name, prob.toDouble())
}