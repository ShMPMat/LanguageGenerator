package shmp.language

import java.text.ParseException

data class Phoneme(val sound : String, val type: PhonemeType) {
    override fun toString(): String {
        return sound
    }
}

