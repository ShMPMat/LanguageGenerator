package shmp.language.morphem

import shmp.language.Word

interface WordChange {
    val position: Position?

    fun change(word: Word): Word
}