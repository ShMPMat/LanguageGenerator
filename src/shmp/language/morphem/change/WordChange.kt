package shmp.language.morphem.change

import shmp.language.lexis.Word

interface WordChange {
    val position: Position?

    fun test(word: Word): Boolean

    fun change(word: Word): Word
}