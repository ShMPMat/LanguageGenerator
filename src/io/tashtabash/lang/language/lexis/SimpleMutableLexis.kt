package io.tashtabash.lang.language.lexis


open class SimpleMutableLexis: AbstractLexis() {
    private val _words = mutableListOf<Word>()

    override val words: List<Word> = _words

    operator fun plusAssign(word: Word) {
        _words += injectTags(word)
    }

    operator fun plusAssign(newWords: Collection<Word>) {
        _words += newWords.map { injectTags(it) }
    }

    private fun injectTags(word: Word): Word {
        return injectVerbObjectTags(word)
    }

    private fun injectVerbObjectTags(word: Word): Word {
        if (word.semanticsCore.speechPart.type != SpeechPart.Verb)
            return word

        return word.copy(
            semanticsCore = word.semanticsCore.copy(
                tags = word.semanticsCore.tags + SemanticsTag("location")
            )
        )
    }
}
