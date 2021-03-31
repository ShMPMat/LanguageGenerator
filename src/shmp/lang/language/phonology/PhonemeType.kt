package shmp.lang.language.phonology

import java.text.ParseException


enum class PhonemeType(val char: Char) {
    Consonant('C'),
    Vowel('V')
}

fun Char.toPhonemeType() = PhonemeType.values().find { it.char == this }
    ?: throw ParseException("No phoneme type exist for Char $this", 0)
