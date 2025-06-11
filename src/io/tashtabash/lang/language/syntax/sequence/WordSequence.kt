package io.tashtabash.lang.language.syntax.sequence

import io.tashtabash.lang.language.lexis.Word


data class WordSequence(val words: List<Word> = listOf()) {
    constructor(vararg words: Word): this(words.toList())
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

    override fun toString() = words.joinToString(" ") {
        it.getPhoneticRepresentation()
    }
}


fun List<Word>.toWordSequence() = WordSequence(this)

fun Word.toWordSequence() = WordSequence(this)
