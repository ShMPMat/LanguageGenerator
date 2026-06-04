package io.tashtabash.lang.generator.util

import io.tashtabash.lang.language.category.CategorySource
import io.tashtabash.lang.language.category.paradigm.*
import io.tashtabash.lang.language.lexis.TypedSpeechPart
import io.tashtabash.lang.language.syntax.SyntaxRelation
import io.tashtabash.random.singleton.chanceOf
import io.tashtabash.random.singleton.testProbability


fun copyApplicators(
    cluster: ExponenceCluster,
    applicator: CategoryHandler,
    sourceMap: Map<SyntaxRelation, List<SyntaxRelation>>,
    speechPartChangeParadigm: SpeechPartChangeParadigm,
    applicatorIdx: Int
): Pair<ExponenceCluster, CategoryHandler> {
    var isSourceChanged = false
    val newCategories = cluster.categories.map {
        it to it.copy(
            source = when (it.source) {
                is CategorySource.Agreement -> {
                    isSourceChanged = isSourceChanged || it.source.relation.any { r -> sourceMap.containsKey(r) }
                    it.source.copy(relation = it.source.relation.flatMap { r -> sourceMap.getOrDefault(r, listOf(r)) })
                }
                is CategorySource.Self -> CategorySource.Self
            }
        )
    }
    // Create a look-up map for substituting old categories for new
    val newCategoriesMap = newCategories.toMap()

    if (!isSourceChanged)
        return cluster to LinkCategoryHandler(speechPartChangeParadigm, applicatorIdx)
    val newClusterValues = cluster.possibleValues.map { v ->
        v.categoryValues.map { cv ->
            newCategoriesMap.getValue(cv.parent).actualSourcedValues.first { cv.categoryValue == it.categoryValue }
        }
    }.toSet()
    val newCluster = ExponenceCluster(newCategories.map { it.second }, newClusterValues)
    val newApplicator = when (applicator) {
        is SyntheticCategoryHandler -> SyntheticCategoryHandler(
            MapApplicatorSource(
                applicator.applicatorSource.map
                    .map { (value, applicator) ->
                        val newValue = newCluster.possibleValues.first { nv ->
                            val newPlainCategoryValues = nv.categoryValues
                                .map { it.categoryValue }
                            value.categoryValues.all { c -> c.categoryValue in newPlainCategoryValues }
                        }

                        newValue to applicator.copy()
                    }
            )
        )
        is LinkCategoryHandler -> applicator
        else -> throw ChangeException("Unknown ApplicatorSource type ${applicator::class.simpleName}")
    }

    return newCluster to newApplicator
}

fun SpeechPartChangeParadigm.copyForNewSpeechPart(
    speechPart: TypedSpeechPart = this.speechPart,
    sourceMap: Map<SyntaxRelation, List<SyntaxRelation>> = mapOf(),
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
