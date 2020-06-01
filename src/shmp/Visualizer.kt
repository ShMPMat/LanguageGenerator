package shmp

import shmp.containers.WordBase
import shmp.generator.LanguageGenerator
import shmp.language.Language
import shmp.language.syntax.Clause
import shmp.language.syntax.Sentence
import shmp.language.syntax.SentenceNode

fun visualize(language: Language) {
//    val verb =
//    val testSentence = Sentence(
//        SentenceNode(Clause())
//    )
    print(language)
}

fun main() {
    visualize(LanguageGenerator(158).generateLanguage(WordBase().words.size))
}