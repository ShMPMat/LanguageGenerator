package shmp.lang.language.morphem

import shmp.lang.language.lexis.Word
import shmp.lang.language.morphem.change.WordChange

interface Affix: WordChange {
    val templateChange: WordChange

    override fun test(word: Word): Boolean = templateChange.test(word)

    override fun change(word: Word) = templateChange.change(word)
}