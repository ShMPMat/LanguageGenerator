package shmp.language.morphem

import shmp.language.Word
import shmp.language.morphem.change.WordChange

interface Affix: WordChange {
    val templateChange: WordChange

    override fun test(word: Word): Boolean = templateChange.test(word)

    override fun change(word: Word) = templateChange.change(word)
}