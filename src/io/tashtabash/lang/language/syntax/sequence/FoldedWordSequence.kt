package io.tashtabash.lang.language.syntax.sequence

import io.tashtabash.lang.language.lexis.Word


data class FoldedWordSequence(val words: List<LatchedWord> = listOf()) {
    constructor(latchedWord: LatchedWord): this(listOf(latchedWord))

    val size = words.size

    operator fun get(position: Int) = words[position]

    operator fun plus(that: FoldedWordSequence) = FoldedWordSequence(this.words + that.words)

    fun mapIndexed(transform: (Int, Word) -> Word) = words
        .mapIndexed { i, (w, l) -> LatchedWord(transform(i, w), l) }
        .toFoldedWordSequence()

    fun swapWord(i: Int, transform: (Word) -> Word) = mapIndexed { j, w ->
        if (i == j) transform(w)
        else w
    }
}
