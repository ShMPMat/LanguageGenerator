package io.tashtabash.lang.language.syntax.sequence

import io.tashtabash.lang.language.lexis.Word
import io.tashtabash.lang.language.syntax.sequence.LatchType.*


enum class LatchType {
    Center,
    InPlace,
    ClauseLatch
}

fun List<FoldedWordSequence>.unfold(center: Int): WordSequence {
    val resultWords = mutableListOf<Word>()
    val prefixWords = mutableListOf<Word>()
    val suffixWords = mutableListOf<Word>()

    for ((i, sequence) in this.withIndex()) {
        if (i != center) {
            resultWords += sequence.words.map { it.word }
            continue
        }

        var isPrefix = true

        for ((word, type) in sequence.words)
            when (type) {
                Center -> {
                    isPrefix = false
                    resultWords += word
                }
                InPlace -> resultWords += word
                ClauseLatch ->
                    if (isPrefix)
                        prefixWords += word
                    else
                        suffixWords += word
            }
    }

    return WordSequence(prefixWords + resultWords + suffixWords)
}


fun List<LatchedWord>.toFoldedWordSequence() = FoldedWordSequence(this)
fun WordSequence.setInPlace() = FoldedWordSequence(words.map { LatchedWord(it, InPlace) })

fun FoldedWordSequence.unfold() = listOf(this).unfold(0)
