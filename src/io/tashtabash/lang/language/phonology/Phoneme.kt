package io.tashtabash.lang.language.phonology

import kotlin.math.abs


data class Phoneme(
    val symbol : String,
    val type: PhonemeType,
    val articulationPlace: ArticulationPlace,
    val articulationManner: ArticulationManner
) {
    override fun toString() = symbol

    fun calculateDistance(other: Phoneme) =
        abs(articulationPlace.positionIndex - other.articulationPlace.positionIndex) +
        abs(articulationManner.positionIndex - other.articulationManner.positionIndex)
}


fun doesPhonemesCollide(firstPhoneme: Phoneme, secondPhoneme: Phoneme) =
    firstPhoneme == secondPhoneme

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
    Uvular(9),
    Pharyngeal(10),
    Glottal(11),

    Front(0),
    Central(3),
    Back(5)
}

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
