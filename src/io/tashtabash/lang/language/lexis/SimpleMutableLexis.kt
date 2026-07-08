package io.tashtabash.lang.language.lexis


open class SimpleMutableLexis(vararg words: Word): AbstractLexis() {
    private val _words = words.toMutableList()

    override val words: List<Word> = _words

    operator fun plusAssign(word: Word) {
        if (words.none { it.softEq(word) }) // Add only if the word is meaningfully different
            _words += injectTags(word)
    }

    operator fun plusAssign(newWords: Collection<Word>) {
        newWords.forEach { this += it }
    }

    fun swap(meaning: Meaning, word: Word) {
        val i = words.indexOfFirst { meaning in it.semanticsCore.meaningCluster }
        if (i == -1)
            return

        _words[i] = word
    }

    private fun injectTags(word: Word): Word {
        return injectVerbObjectTags(word)
    }

    private fun injectVerbObjectTags(word: Word): Word {
        if (word.semanticsCore.speechPart.type != SpeechPart.Verb)
            return word

        return word.copy(
            semanticsCore = word.semanticsCore.copy(
                tags = word.semanticsCore.tags + universalVerbTags
            )
        )
    }

    private val universalVerbTags = listOf(
        SemanticsTag("location"),
        SemanticsTag("benefactor")
    )
}
