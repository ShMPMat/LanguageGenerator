package io.tashtabash.lang.language.phonology

import kotlin.math.abs


data class Phoneme(
    val symbol : String,
    val type: PhonemeType,
    val articulationPlace: ArticulationPlace,
    val articulationManner: ArticulationManner,
    val modifiers: Set<PhonemeModifier> = setOf()
) {
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

enum class ArticulationPlace(val positionIndex: Int) {
    Bilabial(0),
    LabioDental(1),
    Linguolabial(2),
    Dental(3),
    Alveolar(4),
    Postalveolar(5),
    Retroflex(6),
    Palatal(7),
    Velar(8),
    LabialVelar(9),
    Uvular(10),
    Pharyngeal(11),
    Glottal(12),

    Front(0),
    Central(3),
    Back(5)
}

val vowelArticulationPlaces = listOf(ArticulationPlace.Front, ArticulationPlace.Central, ArticulationPlace.Back)
val consonantArticulationPlaces = ArticulationPlace.values()
    .filter { it !in vowelArticulationPlaces }

//TODO positions for consonants aren't final
enum class ArticulationManner(val sonorityLevel: Int, val positionIndex: Int) {
    Nasal(2, 0),
    Stop(5, 0),
    SibilantAffricate(4, 0),
    NonSibilantAffricate(4, 0),
    SibilantFricative(3, 0),
    NonSibilantFricative(3, 0),
    Approximant(1, 0),
    Tap(1, 0),//TODO not found
    Trill(1, 0),//TODO not found
    LateralAffricate(4, 0),
    LateralFricative(3, 0),
    LateralApproximant(1, 0),
    LateralTap(1, 0),//TODO not found

    Close(0, 0),
    NearClose(0, 1),
    CloseMid(0, 2),
    Mid(0, 3),
    OpenMid(0, 4),
    NearOpen(0, 5),
    Open(0, 6)
}

enum class PhonemeModifier {
    // Vowel
    Nasalized,

    // Vowel, Consonant
    Long,
    Voiced,
    Labialized,

    // Consonant
    Palatilized,
    Velarized;

    companion object {
        fun values(type: PhonemeType) = when (type) {
            PhonemeType.Consonant -> listOf(Nasalized, Long, Voiced, Labialized)
            PhonemeType.Vowel -> listOf(Long, Voiced, Labialized, Palatilized, Velarized)
        }
    }
}
