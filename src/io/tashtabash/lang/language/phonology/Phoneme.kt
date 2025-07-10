package io.tashtabash.lang.language.phonology

import kotlin.math.abs


data class Phoneme(
    val symbol : String,
    val type: PhonemeType,
    val articulationPlace: ArticulationPlace,
    val articulationManner: ArticulationManner,
    val modifiers: Set<PhonemeModifier> = setOf()
) {
    val characteristics: Set<PhonemeCharacteristic> = modifiers + listOf(articulationPlace, articulationManner)

    fun contains(vararg characteristic: PhonemeCharacteristic): Boolean =
        characteristics.containsAll(characteristics)

    fun isEqualByProperties(that: Phoneme) =
        copy(symbol = "_") == that.copy(symbol = "_")

    override fun toString() = symbol
}


fun doPhonemesCollide(first: Phoneme, second: Phoneme): Boolean =
    first == second

fun calculateDistance(first: Phoneme, second: Phoneme): Int =
    abs(first.articulationPlace.positionIndex - second.articulationPlace.positionIndex) +
            abs(first.articulationManner.positionIndex - second.articulationManner.positionIndex) +
            (first.modifiers - second.modifiers).size + (second.modifiers - first.modifiers).size
