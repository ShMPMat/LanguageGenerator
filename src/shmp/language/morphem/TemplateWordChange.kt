package shmp.language.morphem

import shmp.language.*
import shmp.language.phonology.Phoneme
import shmp.language.phonology.PhonemeSequence

class TemplateWordChange(val changes: List<TemplateChange>) : WordChange {
    override fun change(word: Word): Word {
        for (changeTemplate in changes) {
            val changedWord = changeTemplate.change(word)
            if (changedWord.toString() != word.toString())
                return changedWord
        }
        return word.copy()
    }

    override fun toString(): String {
        return changes.joinToString()
    }


}

class TemplateChange(
    val position: Position,
    val phonemes: List<PositionTemplate>,
    val result: List<PositionSubstitution>
) {
    //TODO make an interface
    fun test(word: Word): Int {
        return when (position) {
            Position.Beginning -> if (testFromPosition(word, 0)) phonemes.size else -1
            Position.End -> {
                val sublistStart = word.size - phonemes.size
                if (testFromPosition(word, sublistStart)) sublistStart else -1
            }
        }
    }

    private fun testFromPosition(word: Word, position: Int) =
        word.toPhonemes().subList(position, position + phonemes.size).zip(phonemes).all { it.second.test(it.first) }

    fun change(word: Word): Word {
        val testResult = test(word)
        if (testResult != -1) {
            return when(position) {
                Position.End -> {
                    val change = result.zip(testResult until testResult + result.size).map { it.first.substitute(word, it.second) }
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
                    val change = result.zip(testResult - result.size until testResult).map { it.first.substitute(word, it.second) }
                    return word.syllableTemplate.createWord(
                        PhonemeSequence(
                            change + word.toPhonemes().subList(
                                phonemes.size,
                                word.size
                            )
                        ),
                        word.syntaxCore
                    ) ?: throw LanguageException("Couldn't convert $word with change $this to word")
                }//TODO test
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

interface PositionTemplate {
    fun test(phoneme: Phoneme): Boolean
}

class PhonemeTemplate(val phoneme: Phoneme): PositionTemplate {
    override fun test(phoneme: Phoneme) = this.phoneme == phoneme

    override fun toString(): String {
        return phoneme.toString()
    }
}

class TypePositionTemplate(val type: PhonemeType): PositionTemplate {
    override fun test(phoneme: Phoneme) = type == phoneme.type

    override fun toString(): String {
        return type.char.toString()
    }
}

interface PositionSubstitution {
    fun substitute(word: Word, position: Int): Phoneme
}

class PhonemePositionSubstitution(val phoneme: Phoneme): PositionSubstitution {
    override fun substitute(word: Word, position: Int) = phoneme

    override fun toString(): String {
        return phoneme.toString()
    }
}

class PassingPositionSubstitution: PositionSubstitution {
    override fun substitute(word: Word, position: Int) =
        if (position < word.size && position >= 0) word[position]
        else throw LanguageException("Tried to change nonexistent phoneme on position $position in the word $word")

    override fun toString(): String {
        return "_"
    }


}

enum class Position {
    Beginning,
    End
//    Middle,
//    Anywhere
}