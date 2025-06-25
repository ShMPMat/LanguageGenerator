package io.tashtabash.lang.generator.util

import io.tashtabash.lang.generator.ValueMap
import io.tashtabash.lang.language.category.CategorySource
import io.tashtabash.lang.language.category.paradigm.ExponenceCluster
import io.tashtabash.lang.language.category.paradigm.SpeechPartChangeParadigm
import io.tashtabash.lang.language.lexis.TypedSpeechPart
import io.tashtabash.lang.language.syntax.SyntaxRelation
import io.tashtabash.random.singleton.chanceOf
import io.tashtabash.random.singleton.testProbability


fun copyApplicators(
    cluster: ExponenceCluster,
    applicator: ValueMap,
    sourceMap: Map<SyntaxRelation, SyntaxRelation>
): Pair<ExponenceCluster, ValueMap> {
    val newCategories = cluster.categories.map {
        it.copy(
            source = when (it.source) {
                is CategorySource.Agreement ->
                    if (it.source.relation in sourceMap.keys)
                        it.source.copy(relation = sourceMap.getValue(it.source.relation))
                    else it.source.copy()
                is CategorySource.Self -> CategorySource.Self
            }
        )
    }
    val newClusterValues = cluster.possibleValues.map { v ->
        v.categoryValues.map { cv ->
            newCategories.flatMap { it.actualSourcedValues }.first { cv.categoryValue == it.categoryValue }
        }
    }.toSet()
    val newCluster = ExponenceCluster(newCategories, newClusterValues)

    return newCluster to applicator.map { (value, applicator) ->
        val newValue = newCluster.possibleValues.first { nv ->
            value.categoryValues.all { c -> c.categoryValue in nv.categoryValues.map { it.categoryValue } }
        }

        newValue to applicator.copy()
    }
}

fun SpeechPartChangeParadigm.copyForNewSpeechPart(
    speechPart: TypedSpeechPart = this.speechPart,
    sourceMap: Map<SyntaxRelation, SyntaxRelation> = mapOf(),
    clusterPredicate: (ExponenceCluster) -> Boolean
): SpeechPartChangeParadigm {
    val newApplicators = exponenceClusters
        .mapNotNull { cluster ->
            val applicator = applicators.getValue(cluster)

            if (!clusterPredicate(cluster))
                return@mapNotNull null

            copyApplicators(cluster, applicator, sourceMap)
        }

    return SpeechPartChangeParadigm(
        speechPart,
        newApplicators.map { it.first },
        newApplicators.toMap(),
        prosodyChangeParadigm.copy()
    )
}

fun SpeechPartChangeParadigm.substituteWith(from: SpeechPartChangeParadigm) =
    0.9.chanceOf<SpeechPartChangeParadigm> { fullSubstituteWith(from) }
        ?: partialSubstituteWith(from)


private fun SpeechPartChangeParadigm.fullSubstituteWith(from: SpeechPartChangeParadigm): SpeechPartChangeParadigm {
    val copy = from.copyForNewSpeechPart { getCluster(it) != null }

    return combineParadigms(this, copy)
}

private fun SpeechPartChangeParadigm.partialSubstituteWith(from: SpeechPartChangeParadigm): SpeechPartChangeParadigm {
    val copy = from.copyForNewSpeechPart { getCluster(it) != null && 0.5.testProbability() }

    return combineParadigms(this, copy)
}

private fun combineParadigms(old: SpeechPartChangeParadigm, new: SpeechPartChangeParadigm): SpeechPartChangeParadigm {
    val newOrder = old.exponenceClusters.map { new.getCluster(it) ?: it }
    val newApplicators = newOrder.map {
        it to (new.applicators[it] ?: old.applicators.getValue(it))
    }.toMap()

    return old.copy(exponenceClusters = newOrder, applicators = newApplicators)
}

//private fun hasSameCategorySet(first: SpeechPartChangeParadigm, second: SpeechPartChangeParadigm) =
//    first.exponenceClusters.size == second.exponenceClusters.size && first.exponenceClusters.all { cluster ->
//        second.getCluster(cluster) != null
//    }
