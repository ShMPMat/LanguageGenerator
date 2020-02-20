package shmp.containers

import shmp.language.SpeechPart
import shmp.language.SyntaxCore
import java.io.File

class WordBase() {
    val words: MutableList<SyntaxCoreTemplate> = ArrayList()

    init {
        File("SupplementFiles/Words").forEachLine {
            if (!it.isBlank() && it[0] != '/') {
                val tokens = it.split(" +".toRegex())
                val word = tokens[0]
                val speechPart = SpeechPart.valueOf(tokens[1])
                words.add(SyntaxCoreTemplate(word, speechPart))
            }
        }
    }
}

data class SyntaxCoreTemplate(val word: String, val speechPart: SpeechPart)