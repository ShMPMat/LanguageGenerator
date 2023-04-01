package io.tashtabash.lang.language.category.value

import io.tashtabash.lang.language.category.realization.CategoryRealization
import io.tashtabash.random.UnwrappableSSO


data class RealizationBox(
    val realization: CategoryRealization?,
    override val probability: Double
) : UnwrappableSSO<CategoryRealization?>(realization) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RealizationBox

        if (realization != other.realization) return false

        return true
    }

    override fun hashCode() = realization?.hashCode() ?: 0
}
