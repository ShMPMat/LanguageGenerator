package io.tashtabash.lang.language.syntax.sequence

import io.tashtabash.lang.language.lexis.Word


data class FoldedWordSequence(val words: List<Pair<Word, LatchType>> = listOf()) {
    constructor(wordToType: Pair<Word, LatchType>): this(listOf(wordToType))

    val size = words.size

    operator fun get(position: Int) = words[position]

    operator fun plus(that: FoldedWordSequence) = FoldedWordSequence(this.words + that.words)

    fun mapIndexed(transform: (Int, Word) -> Word) = words
        .mapIndexed { i, (w, l) -> transform(i, w) to l }
        .toFoldedWordSequence()

    fun swapWord(i: Int, transform: (Word) -> Word) = mapIndexed { j, w ->
        if (i == j) transform(w)
        else w
    }
}
