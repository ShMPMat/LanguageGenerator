package io.tashtabash.lang.language.lexis

import io.tashtabash.lang.containers.SemanticsCoreTemplate

sealed class SemanticsCoreMatcher {
    abstract fun match(core: SemanticsCoreTemplate): Boolean
}

object PassingMatcher: SemanticsCoreMatcher() {
    override fun match(core: SemanticsCoreTemplate) = true
}

data class SpeechPartMatcher(private val speechPart: SpeechPart): SemanticsCoreMatcher() {
    override fun match(core: SemanticsCoreTemplate) = core.speechPart == speechPart
}

data class TagMatcher(private val tag: SemanticsTag): SemanticsCoreMatcher() {
    override fun match(core: SemanticsCoreTemplate) = core.tagClusters
        .any { it.semanticsTags.size == 1 && it.semanticsTags[0].name == tag.name }
}

class ConcatMatcher(vararg matchers: SemanticsCoreMatcher): SemanticsCoreMatcher() {
    private val _matchers = matchers.toList()

    override fun match(core: SemanticsCoreTemplate) = _matchers.all { it.match(core) }
}