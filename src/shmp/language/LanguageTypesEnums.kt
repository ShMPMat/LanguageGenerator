package shmp.language

import java.text.ParseException


enum class PhonemeType(val char: Char) {
    Consonant('C'),
    Vowel('V')
}

fun Char.toPhonemeType() = PhonemeType.values().find { it.char == this }
    ?: throw ParseException("No phoneme type exist for Char $this", 0)

enum class VowelQualityAmount(val amount: Int, val probability: Double) {
    Two(2, 4.0),
    Three(3, 30.0),//No actual data
    Four(4, 49.0),//No actual data
    Five(5, 188.0),
    Six(6, 100.0),
    Seven(7, 60.0),//No actual data
    Eight(8, 31.0),//No actual data
    Nine(9, 20.0),//No actual data
    Ten(10, 10.0),//No actual data
    Eleven(11, 6.0),//No actual data
    Twenty(12, 4.0),//No actual data
    Thirteen(13,2.0),
    Fourteen(14, 1.0)
}

enum class Stress(val probability: Double) {
    NotFixed(220.0),
    Initial(92.0),
    Second(16.0),
    Third(1.0),
    Antepenultimate(12.0),
    Penultimate(110.0),
    Ultimate(51.0)
}

enum class SovOrder(val probability: Double) {
    SOV(565.0),
    SVO(488.0),
    VSO(95.0),
    VOS(25.0),
    OVS(11.0),
    OSV(4.0),
    None(189.0)
}

enum class SpeechPart {
    Noun,
    Verb,
    Adjective,
    Adverb,
    Numeral,
    Article,
    Pronoun
}

enum class NominalCategoryRealization {
    PrefixSeparateWord,
    SuffixSeparateWord,
    Prefix,
    Suffix
}

interface NominalCategoryEnum {//TODO not Nominal
    val syntaxCore: SyntaxCore
}
