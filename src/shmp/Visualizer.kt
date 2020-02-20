package shmp

import shmp.generator.Generator
import shmp.language.Language

fun visualize(language: Language) {
    print(language)
}

fun main() {
    visualize(Generator(78).generateLanguage( 40))
}