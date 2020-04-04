package shmp.language.phonology

import shmp.language.PhonemeType

data class Phoneme(
    val sound : String,
    val type: PhonemeType,
    val articulationPlace: ArticulationPlace,
    val articulationManner: ArticulationManner
) {
    override fun toString(): String {
        return sound
    }
}

fun doesPhonemesCollide(firstPhoneme: Phoneme, secondPhoneme: Phoneme) =
    firstPhoneme == secondPhoneme

enum class ArticulationPlace {
    Bilabial,
    LabioDental,
    Linguolabial,
    Dental,
    Alveolar,
    Postalveolar,
    Retroflex,
    Palatal,
    Velar,
    Uvular,
    Pharyngeal,
    Glottal,

    Front,
    Central,
    Back
}

enum class ArticulationManner(sonorityLevel: Int) {
    Nasal(2),
    Stop(5),
    SibilantAffricate(4),
    NonSibilantAffricate(4),
    SibilantFricative(3),
    NonSibilantFricative(3),
    Approximant(1),
    Tap(1),//TODO not found
    Trill(1),//TODO not found
    LateralAffricate(4),
    LateralFricative(3),
    LateralApproximant(1),
    LateralTap(1),//TODO not found

    Close(0),
    NearClose(0),
    CloseMid(0),
    OpenMid(0),
    Open(0)
}
