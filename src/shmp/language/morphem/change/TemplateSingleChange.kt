package shmp.language.morphem.change

import shmp.language.*
import shmp.language.phonology.Phoneme
import shmp.language.phonology.PhonemeSequence

class TemplateSingleChange(
    override val position: Position,
    val phonemeMatchers: List<PositionMatcher>,
    val matchedPhonemesSubstitution: List<PositionSubstitution>,
    val affix: List<PositionSubstitution>//TODO split on two parts
): WordChange {
    //TODO make an interface
    fun findGoodIndex(word: Word): Int? {
        return when (position) {
            Position.Beginning -> if (testFromPosition(word, 0)) phonemeMatchers.size else null
            Position.End -> {
                val sublistStart = word.size - phonemeMatchers.size
                if (testFromPosition(word, sublistStart)) sublistStart else null
            }
        }
    }

    override fun test(word: Word): Boolean = findGoodIndex(word) != null

    private fun testFromPosition(word: Word, position: Int) =
        word.toPhonemes().subList(position, position + phonemeMatchers.size).zip(phonemeMatchers).all { it.second.test(it.first) }

    fun getFullChange() = when(position) {
        Position.Beginning -> affix + matchedPhonemesSubstitution
        Position.End -> matchedPhonemesSubstitution + affix
    }

    override fun change(word: Word): Word {
        val testResult = findGoodIndex(word)
        if (testResult != null) {
            return when (position) {
                Position.End -> {
                    val change = getFullChange().zip(testResult until testResult + matchedPhonemesSubstitution.size + affix.size)
                        .map { it.first.substitute(word, it.second) }
                    return word.syllableTemplate.createWord(
                        PhonemeSequence(
                            word.toPhonemes().subList(
                                0,
                                word.size - phonemeMatchers.size
                            ) + change
                        ),
                        word.syntaxCore
                    ) ?: throw LanguageException("Couldn't convert $word with change $this to word")
                }
                Position.Beginning -> {
                    val change = getFullChange().zip(testResult - matchedPhonemesSubstitution.size - affix.size until testResult)
                        .map { it.first.substitute(word, it.second) }
                    return word.syllableTemplate.createWord(
                        PhonemeSequence(
                            change + word.toPhonemes().subList(
                                phonemeMatchers.size,
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
        return "$position ${if (phonemeMatchers.isEmpty()) "with any phonemes" else phonemeMatchers.joinToString("")} " +
                "changes to ${getFullChange().joinToString("")}"
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
    fun getSubstitutePhoneme(): Phoneme?
}

class PhonemePositionSubstitution(val phoneme: Phoneme) :
    PositionSubstitution {
    override fun substitute(word: Word, position: Int) = phoneme
    override fun getSubstitutePhoneme(): Phoneme? = phoneme

    override fun toString(): String {
        return phoneme.toString()
    }
}

class PassingPositionSubstitution : PositionSubstitution {
    override fun substitute(word: Word, position: Int) =
        if (position < word.size && position >= 0) word[position]
        else throw LanguageException("Tried to change nonexistent phoneme on position $position in the word $word")

    override fun getSubstitutePhoneme(): Phoneme? = null

    override fun toString(): String {
        return "_"
    }
}
