package io.tashtabash.lang.language.morphem.change

import io.tashtabash.lang.language.lexis.Word


interface WordChange {
    val position: Position?

    fun test(word: Word): Boolean

    fun change(word: Word): Word
}
