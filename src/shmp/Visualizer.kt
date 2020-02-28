package shmp

import shmp.generator.LanguageGenerator
import shmp.language.Language

fun visualize(language: Language) {
    print(language)
}

fun main() {
    visualize(LanguageGenerator(106).generateLanguage( 40))//105 had a lot of categories 106 there was a category with a redundant variant
}