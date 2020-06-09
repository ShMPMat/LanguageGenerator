package shmp.language

import shmp.language.lexis.SemanticsCore
import shmp.language.syntax.SyntaxRelation
import shmp.language.syntax.SyntaxRelation.*
import shmp.random.SampleSpaceObject
import java.text.ParseException


enum class PhonemeType(val char: Char) {
    Consonant('C'),
    Vowel('V')
}

fun Char.toPhonemeType() = PhonemeType.values().find { it.char == this }
    ?: throw ParseException("No phoneme type exist for Char $this", 0)

enum class VowelQualityAmount(val amount: Int, override val probability: Double) : SampleSpaceObject {
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
    Thirteen(13, 2.0)
}

enum class NumeralSystemBase(override val probability: Double) : SampleSpaceObject {
    Decimal( 125.0),
    Vigesimal( 20.0),
    VigesimalTill100( 22.0),
    SixtyBased(5.0),
    ExtendedBodyPartSystem(4.0),
    Restricted3(6.0),
    Restricted5(6.0),
    Restricted20(6.0)
}

enum class SpeechPart {
    Noun,
    Verb,
    Adjective,
    Adverb,
    Numeral,
    Article,
    Particle,
    Pronoun
}

enum class CategoryRealization {
    NewWord,
    PrefixSeparateWord,
    SuffixSeparateWord,
    Prefix,
    Suffix,
    Reduplication,
    Passing
}

interface CategoryValue {
    val semanticsCore: SemanticsCore
    val parentClassName: String
}
