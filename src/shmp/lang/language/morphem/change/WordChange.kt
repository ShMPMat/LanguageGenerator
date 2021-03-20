package shmp.lang.language.morphem.change

import shmp.lang.language.lexis.Word

interface WordChange {
    val position: Position?

    fun test(word: Word): Boolean

    fun change(word: Word): Word
}