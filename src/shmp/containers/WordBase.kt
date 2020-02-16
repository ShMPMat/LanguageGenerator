package shmp.containers

import shmp.language.SpeechPart
import shmp.language.SyntaxCore
import java.io.File

class WordBase() {
    val words: MutableList<SyntaxCore> = ArrayList()

    init {
        File("SupplementFiles/Words").forEachLine {
            if (!it.isBlank() && it[0] != '/') {
                val tokens = it.split(" +".toRegex())
                val word = tokens[0]
                val speechPart = SpeechPart.valueOf(tokens[1])
                words.add(SyntaxCore(word, speechPart))
            }
        }
    }
}