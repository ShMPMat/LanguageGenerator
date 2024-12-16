package io.tashtabash.lang.language.syntax.sequence

import io.tashtabash.lang.language.lexis.Word


data class LatchedWord(val word: Word, val latchType: LatchType) {
    fun map(mapper: (Word) -> Word): LatchedWord =
        copy(word = mapper(word))
}
