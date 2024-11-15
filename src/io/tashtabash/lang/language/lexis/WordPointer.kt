package io.tashtabash.lang.language.lexis


interface WordPointer {
    fun resolve(lexis: AbstractLexis): Word
}

class SimpleWordPointer(internal val word: Word): WordPointer {
    override fun resolve(lexis: AbstractLexis): Word =
        word
}

class IndexWordPointer(private val wordIdx: Int): WordPointer {
    // 'lexis' must have the same order of words as the Lexis for which
    // this pointer was created. The burden of supporting this invariant
    // is on the user of the pointers and lexis.
    override fun resolve(lexis: AbstractLexis): Word =
        lexis.words[wordIdx]
}
