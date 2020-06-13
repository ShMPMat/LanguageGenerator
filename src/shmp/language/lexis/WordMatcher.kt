package shmp.language.lexis

import shmp.language.SpeechPart

sealed class WordMatcher {
    abstract fun match(word: Word): Boolean
}

object PassingMatcher: WordMatcher() {
    override fun match(word: Word) = true
}

data class SpeechPartMatcher(private val speechPart: SpeechPart): WordMatcher() {
    override fun match(word: Word) = word.semanticsCore.speechPart == speechPart
}

data class TagMatcher(private val tag: SemanticsTag): WordMatcher() {
    override fun match(word: Word) = word.semanticsCore.tags.contains(tag)
}

class ConcatMatcher(vararg matchers: WordMatcher): WordMatcher() {
    private val _matchers = matchers.toList()

    override fun match(word: Word) = _matchers.all { it.match(word) }
}