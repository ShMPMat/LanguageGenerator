package io.tashtabash.lang.language.lexis


interface WordPointer {
    fun resolve(lexis: Lexis): Word
}

class SimpleWordPointer(internal val word: Word): WordPointer {
    override fun resolve(lexis: Lexis): Word =
        word
}

class IndexWordPointer(private val wordIdx: Int): WordPointer {
    override fun resolve(lexis: Lexis): Word =
        lexis.words[wordIdx]
}
