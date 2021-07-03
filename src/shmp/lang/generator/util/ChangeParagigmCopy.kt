package shmp.lang.generator.util

import shmp.lang.language.category.CategorySource
import shmp.lang.language.category.paradigm.ExponenceCluster
import shmp.lang.language.category.paradigm.ExponenceValue
import shmp.lang.language.category.realization.CategoryApplicator
import shmp.lang.language.syntax.SyntaxRelation


fun copyApplicators(
    cluster: ExponenceCluster,
    applicator: Map<ExponenceValue, CategoryApplicator>,
    sourceMap: Map<SyntaxRelation, SyntaxRelation>
): Pair<ExponenceCluster, Map<ExponenceValue, CategoryApplicator>> {
    val newCategories = cluster.categories.map {
        it.copy(
            source = when (it.source) {
                is CategorySource.RelationGranted ->
                    if (it.source.relation in sourceMap.keys)
                        it.source.copy(relation = sourceMap.getValue(it.source.relation))
                    else it.source.copy()
                is CategorySource.SelfStated -> CategorySource.SelfStated
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
        val newValue = newCluster.possibleValues
            .first { nv -> value.categoryValues.all { c -> c.categoryValue in nv.categoryValues.map { it.categoryValue } } }

        newValue to applicator.copy()
    }.toMap()
}
