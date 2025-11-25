package io.tashtabash.lang.language.category.paradigm

import io.tashtabash.lang.language.category.realization.CategoryApplicator
import io.tashtabash.lang.language.category.realization.analyticalRealizations


class ApplicatorMap(): LinkedHashMap<ExponenceValue, CategoryApplicator>() {
    constructor(values: List<ExponenceValue>, applicators: List<CategoryApplicator>) : this() {
        for (i in values.indices)
            this[values[i]] = applicators[i]
    }

    constructor(map: Map<ExponenceValue, CategoryApplicator>): this() {
        for ((k, v) in map)
            this[k] = v
    }

    val isAnalytical: Boolean
        get() = values.all { it.type in analyticalRealizations }

    fun map(mapper: (Map.Entry<ExponenceValue, CategoryApplicator>) -> Pair<ExponenceValue, CategoryApplicator>): ApplicatorMap {
        val newMap = ApplicatorMap()

        for (entry: MutableMap.MutableEntry<ExponenceValue, CategoryApplicator> in entries) {
            val (newK, newV) = mapper(entry)
            newMap[newK] = newV
        }

        return newMap
    }
}
