package shmp

import shmp.generator.LanguageGenerator
import shmp.language.Language

fun visualize(language: Language) {
    print(language)
}

fun main() {
    visualize(LanguageGenerator(138).generateLanguage( 40))
}