package shmp.lang.generator.util

import shmp.lang.language.category.CategorySource
import shmp.lang.language.category.paradigm.ExponenceCluster
import shmp.lang.language.category.paradigm.ExponenceValue
import shmp.lang.language.category.paradigm.SpeechPartChangeParadigm
import shmp.lang.language.category.realization.CategoryApplicator
import shmp.lang.language.lexis.TypedSpeechPart
import shmp.lang.language.syntax.SyntaxRelation
import shmp.random.singleton.chanceOf
import shmp.random.singleton.testProbability


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
        val newValue = newCluster.possibleValues.first { nv ->
            value.categoryValues.all { c -> c.categoryValue in nv.categoryValues.map { it.categoryValue } }
        }

        newValue to applicator.copy()
    }.toMap()
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
