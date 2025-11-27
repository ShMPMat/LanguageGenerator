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

    constructor(values: List<ExponenceValue>, applicators: List<CategoryApplicator>) : this() {
        for (i in values.indices)
            map[values[i]] = applicators[i]
    }

    override fun mapApplicators(mapper: (ExponenceValue, CategoryApplicator) -> CategoryApplicator) =
        MapApplicatorSource(
            ApplicatorMap(map.mapValues { (e, a) -> mapper(e, a) })
        )

    override fun copy() = MapApplicatorSource(ApplicatorMap(map))

    override fun toString() = ""
}


data class LinkApplicatorSource(val source: SpeechPartChangeParadigm, val applicatorIdx: Int): ApplicatorSource {
    override val map: ApplicatorMap
        get() = source.applicators[applicatorIdx]
            .second
            .map

    override val originalMap = ApplicatorMap()

    // Don't map anything, if the change is global, the map will be mapped in the source
    override fun mapApplicators(mapper: (ExponenceValue, CategoryApplicator) -> CategoryApplicator) = this

    override fun copy() = LinkApplicatorSource(source, applicatorIdx)

    override fun toString() = " (The same as for ${source.speechPart})"
}
