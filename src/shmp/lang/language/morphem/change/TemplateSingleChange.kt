package shmp.lang.language.morphem.change

import shmp.lang.language.LanguageException
import shmp.lang.language.PhonemeType
import shmp.lang.language.lexis.Word
import shmp.lang.language.phonology.Phoneme
import shmp.lang.language.phonology.PhonemeSequence


class TemplateSingleChange(
    override val position: Position,
    val phonemeMatchers: List<PositionMatcher>,
    val matchedPhonemesSubstitution: List<PositionSubstitution>,
    val affix: List<PositionSubstitution>//TODO split on two parts
) : WordChange {
    //TODO make an interface
    fun findGoodIndex(word: Word): Int? {
        return when (position) {
            Position.Beginning ->
                if (testFromPosition(word, 0))
                    phonemeMatchers.size
                else null
            Position.End -> {
                val sublistStart = word.size - phonemeMatchers.size
                if (testFromPosition(word, sublistStart)) sublistStart else null
            }
        }
    }

    override fun test(word: Word): Boolean = findGoodIndex(word) != null

    private fun testFromPosition(word: Word, position: Int) =
        word.toPhonemes().subList(position, position + phonemeMatchers.size).zip(phonemeMatchers)
            .all { it.second.test(it.first) }

    fun getFullChange() = when (position) {
        Position.Beginning -> affix + matchedPhonemesSubstitution
        Position.End -> matchedPhonemesSubstitution + affix
    }

    override fun change(word: Word): Word {
        fun Word.takeProsody(i: Int) = this.syllables
            .getOrNull(i)
            ?.prosodicEnums
            ?.toList()
            ?: listOf()

        val testResult = findGoodIndex(word)
        if (testResult != null) {
            val prosodicSyllables = when (position) {
                Position.End -> {
                    val change =
                        getFullChange().zip(testResult until testResult + matchedPhonemesSubstitution.size + affix.size)
                            .map { it.first.substitute(word, it.second) }
                    val noProsodyWord = word.syllableTemplate.splitOnSyllables(
                        PhonemeSequence(
                            word.toPhonemes().subList(
                                0,
                                word.size - phonemeMatchers.size
                            ) + change
                        )
                    ) ?: throw LanguageException("Couldn't convert $word with change $this to word")

                    noProsodyWord.mapIndexed { i, s ->
                        s.copy(prosodicEnums = word.takeProsody(i))
                    }
                }
                Position.Beginning -> {
                    val change =
                        getFullChange().zip(testResult - matchedPhonemesSubstitution.size - affix.size until testResult)
                            .map { it.first.substitute(word, it.second) }
                    val noProsodyWord = word.syllableTemplate.splitOnSyllables(
                        PhonemeSequence(
                            change + word.toPhonemes().subList(
                                phonemeMatchers.size,
                                word.size
                            )
                        )
                    ) ?: throw LanguageException("Couldn't convert $word with change $this to word")
                    val shift = noProsodyWord.size - word.syllables.size

                    noProsodyWord.mapIndexed { i, s ->
                        s.copy(prosodicEnums = word.takeProsody(i - shift))
                    }
                }
            }
            return word.copy(syllables = prosodicSyllables)
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

class PassingMatcher() : PositionMatcher {
    override fun test(phoneme: Phoneme): Boolean = true

    override fun toString() = "*"
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

    override fun toString() = "_"
}
