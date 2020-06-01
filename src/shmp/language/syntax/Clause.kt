package shmp.language.syntax

import shmp.language.Word

class Clause(val words: List<Word>) {
    val size: Int = words.size

    operator fun get(position: Int): Word = words[position]

    override fun toString(): String {
        return words.joinToString(" ")
    }


}