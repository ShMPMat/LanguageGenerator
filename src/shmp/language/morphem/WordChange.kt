package shmp.language.morphem

import shmp.language.Word

interface WordChange {
    fun change(word: Word): Word
}