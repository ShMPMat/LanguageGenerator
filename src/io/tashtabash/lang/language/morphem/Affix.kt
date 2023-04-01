package io.tashtabash.lang.language.morphem

import io.tashtabash.lang.language.lexis.Word
import io.tashtabash.lang.language.morphem.change.WordChange

interface Affix: WordChange {
    val templateChange: WordChange

    override fun test(word: Word): Boolean = templateChange.test(word)

    override fun change(word: Word) = templateChange.change(word)
}