package shmp.language.morphem.change

import shmp.language.Word
import shmp.language.morphem.change.Position

interface WordChange {
    val position: Position?

    fun test(word: Word): Boolean

    fun change(word: Word): Word
}