package shmp

import shmp.generator.LanguageGenerator
import shmp.language.Language

fun visualize(language: Language) {
    print(language)
}

fun main() {
    visualize(LanguageGenerator(141).generateLanguage( 40))//TODO 128, 132, 135 - bug
}