package shmp.language.morphem.change

import shmp.language.*
import shmp.language.phonology.Phoneme
import shmp.language.phonology.PhonemeSequence

class TemplateSingleChange(
    override val position: Position,
    val phonemes: List<PositionMatcher>,
    val result: List<PositionSubstitution>
): WordChange {
    //TODO make an interface
    fun findGoodIndex(word: Word): Int? {
        return when (position) {
            Position.Beginning -> if (testFromPosition(word, 0)) phonemes.size else -1
            Position.End -> {
                val sublistStart = word.size - phonemes.size
                if (testFromPosition(word, sublistStart)) sublistStart else null
            }
        }
    }

    override fun test(word: Word): Boolean = findGoodIndex(word) != null

    private fun testFromPosition(word: Word, position: Int) =
        word.toPhonemes().subList(position, position + phonemes.size).zip(phonemes).all { it.second.test(it.first) }

    override fun change(word: Word): Word {
        val testResult = findGoodIndex(word)
        if (testResult != null) {
            return when (position) {
                Position.End -> {
                    val change = result.zip(testResult until testResult + result.size)
                        .map { it.first.substitute(word, it.second) }
                    return word.syllableTemplate.createWord(
                        PhonemeSequence(
                            word.toPhonemes().subList(
                                0,
                                word.size - phonemes.size
                            ) + change
                        ),
                        word.syntaxCore
                    ) ?: throw LanguageException("Couldn't convert $word with change $this to word")
                }
                Position.Beginning -> {
                    val change = result.zip(testResult - result.size until testResult)
                        .map { it.first.substitute(word, it.second) }
                    return word.syllableTemplate.createWord(
                        PhonemeSequence(
                            change + word.toPhonemes().subList(
                                phonemes.size,
                                word.size
                            )
                        ),
                        word.syntaxCore
                    ) ?: throw LanguageException("Couldn't convert $word with change $this to word")
                }
            }
        } else {
            return word.copy()
        }
    }

    override fun toString(): String {
        return "$position ${if (phonemes.isEmpty()) "with any phonemes" else phonemes.joinToString("")} " +
                "changes to ${result.joinToString("")}"
    }
}

interface PositionMatcher {
    fun test(phoneme: Phoneme): Boolean
}

class PhonemeMatcher(val phoneme: Phoneme) : PositionMatcher {
    override fun test(phoneme: Phoneme) = this.phoneme == phoneme

    override fun toString(): String {
        return phoneme.toString()
    }
}

class TypePositionMatcher(val type: PhonemeType) : PositionMatcher {
    override fun test(phoneme: Phoneme) = type == phoneme.type

    override fun toString(): String {
        return type.char.toString()
    }
}

interface PositionSubstitution {
    fun substitute(word: Word, position: Int): Phoneme
}

class PhonemePositionSubstitution(val phoneme: Phoneme) :
    PositionSubstitution {
    override fun substitute(word: Word, position: Int) = phoneme

    override fun toString(): String {
        return phoneme.toString()
    }
}

class PassingPositionSubstitution : PositionSubstitution {
    override fun substitute(word: Word, position: Int) =
        if (position < word.size && position >= 0) word[position]
        else throw LanguageException("Tried to change nonexistent phoneme on position $position in the word $word")

    override fun toString(): String {
        return "_"
    }
}
