package io.tashtabash.lang.language.category.paradigm

import io.tashtabash.lang.language.category.realization.CategoryApplicator


interface ApplicatorSource {
    val map: ApplicatorMap
    val originalMap: ApplicatorMap

    fun mapApplicators(mapper: (ExponenceValue, CategoryApplicator) -> CategoryApplicator) : ApplicatorSource
    fun copy() : ApplicatorSource
}


data class MapApplicatorSource(override val map: ApplicatorMap = ApplicatorMap()) : ApplicatorSource {
    override val originalMap = map

    override fun mapApplicators(mapper: (ExponenceValue, CategoryApplicator) -> CategoryApplicator) =
        MapApplicatorSource(
            ApplicatorMap(map.mapValues { (e, a) -> mapper(e, a) })
        )

    override fun copy() = MapApplicatorSource(ApplicatorMap(map))

    override fun toString() = ""
}
