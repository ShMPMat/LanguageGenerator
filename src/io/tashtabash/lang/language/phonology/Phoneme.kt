package io.tashtabash.lang.language.phonology

import kotlin.math.abs


data class Phoneme(
    val symbol : String,
    val type: PhonemeType,
    val articulationPlace: ArticulationPlace,
    val articulationManner: ArticulationManner,
    val modifiers: Set<PhonemeModifier> = setOf()
) {
    val characteristics = modifiers + listOf(articulationPlace, articulationManner)

    fun isEqualByProperties(that: Phoneme) =
        copy(symbol = "_") == that.copy(symbol = "_")

    override fun toString() = symbol
}


fun doPhonemesCollide(first: Phoneme, second: Phoneme) =
    first == second

fun calculateDistance(first: Phoneme, second: Phoneme) =
    abs(first.articulationPlace.positionIndex - second.articulationPlace.positionIndex) +
            abs(first.articulationManner.positionIndex - second.articulationManner.positionIndex) +
            (first.modifiers - second.modifiers).size + (second.modifiers - first.modifiers).size
