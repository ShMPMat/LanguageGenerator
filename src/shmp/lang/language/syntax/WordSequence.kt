package shmp.lang.language.syntax

import shmp.lang.language.lexis.Word


class WordSequence(val words: List<Word>) {
    val size: Int = words.size

    operator fun get(position: Int): Word = words[position]

    operator fun plus(that: WordSequence) = WordSequence(this.words + that.words)

    override fun toString() = words.joinToString(" ")
}
