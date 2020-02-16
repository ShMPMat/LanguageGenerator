package shmp.language.morphem

import shmp.language.Word

interface Affix: WordChange {
    val templateChange: WordChange

    override fun change(word: Word): Word {
        return templateChange.change(word)
    }
}