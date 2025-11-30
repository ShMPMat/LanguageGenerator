package io.tashtabash.lang.generator.util

import io.tashtabash.lang.language.category.CategorySource
import io.tashtabash.lang.language.category.paradigm.*
import io.tashtabash.lang.language.lexis.TypedSpeechPart
import io.tashtabash.lang.language.syntax.SyntaxRelation
import io.tashtabash.random.singleton.chanceOf
import io.tashtabash.random.singleton.testProbability


fun copyApplicators(
    cluster: ExponenceCluster,
    applicator: ApplicatorSource,
    sourceMap: Map<SyntaxRelation, SyntaxRelation>,
    speechPartChangeParadigm: SpeechPartChangeParadigm,
    applicatorIdx: Int
): Pair<ExponenceCluster, ApplicatorSource> {
    var isSourceChanged = false
    val newCategories = cluster.categories.map {
        it.copy(
            source = when (it.source) {
                is CategorySource.Agreement -> {
                    isSourceChanged = isSourceChanged || sourceMap.containsKey(it.source.relation)
                    it.source.copy(relation = sourceMap.getOrDefault(it.source.relation, it.source.relation))
                }
                is CategorySource.Self -> CategorySource.Self
            }
        )
    }
    if (!isSourceChanged)
        return cluster to LinkApplicatorSource(speechPartChangeParadigm, applicatorIdx)

    val newClusterValues = cluster.possibleValues.map { v ->
        v.categoryValues.map { cv ->
            newCategories.flatMap { it.actualSourcedValues }.first { cv.categoryValue == it.categoryValue }
        }
    }.toSet()
    val newCluster = ExponenceCluster(newCategories, newClusterValues)
    val newApplicator = when (applicator) {
        is MapApplicatorSource -> MapApplicatorSource(
            applicator.map
                .map { (value, applicator) ->
                    val newValue = newCluster.possibleValues.first { nv ->
                        val newPlainCategoryValues = nv.categoryValues
                            .map { it.categoryValue }
                        value.categoryValues.all { c -> c.categoryValue in newPlainCategoryValues }
                    }

                    newValue to applicator.copy()
                }
        )
        is LinkApplicatorSource -> applicator
        else -> throw ChangeException("Unknown ApplicatorSource type ${applicator::class.simpleName}")
    }

    return newCluster to newApplicator
}

fun SpeechPartChangeParadigm.copyForNewSpeechPart(
    speechPart: TypedSpeechPart = this.speechPart,
    sourceMap: Map<SyntaxRelation, SyntaxRelation> = mapOf(),
    clusterPredicate: (ExponenceCluster) -> Boolean = { true }
): SpeechPartChangeParadigm {
    val newApplicators = applicators
        .mapIndexedNotNull { i, (cluster, applicator) ->
            if (!clusterPredicate(cluster))
                return@mapIndexedNotNull null

            copyApplicators(cluster, applicator, sourceMap, this, i)
        }

    return SpeechPartChangeParadigm(
        speechPart,
        newApplicators,
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
    val newApplicators = old.applicators.map { applicator ->
        new.applicators.firstOrNull { it.first == applicator.first } ?: applicator
    }

    return old.copy(applicators = newApplicators)
}
