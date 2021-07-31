package shmp.lang.language.syntax

import shmp.lang.language.lexis.Word


data class WordSequence(val words: List<Word> = listOf()) {
    constructor(word: Word): this(listOf(word))

    val size = words.size

    operator fun get(position: Int) = words[position]

    operator fun plus(that: WordSequence) = WordSequence(this.words + that.words)

    fun mapIndexed(transform: (Int, Word) -> Word) = words.mapIndexed(transform)
        .toWordSequence()

    fun swapWord(i: Int, transform: (Word) -> Word) = mapIndexed { j, w ->
        if (i == j) transform(w)
        else w
    }

    override fun toString() = words.joinToString(" ")
}


fun List<Word>.toWordSequence() = WordSequence(this)
