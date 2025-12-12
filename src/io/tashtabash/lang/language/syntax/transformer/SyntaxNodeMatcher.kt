package io.tashtabash.lang.language.syntax.transformer

import io.tashtabash.lang.language.lexis.SemanticsTag
import io.tashtabash.lang.language.lexis.SpeechPart
import io.tashtabash.lang.language.syntax.SyntaxRelation
import io.tashtabash.lang.language.syntax.clause.syntax.SyntaxNode


interface SyntaxNodeMatcher {
    fun match(node: SyntaxNode): Boolean
}


data class MulMatcher(val matchers: List<SyntaxNodeMatcher>) : SyntaxNodeMatcher {
    override fun match(node: SyntaxNode): Boolean =
        matchers.all { it.match(node) }

    override fun toString(): String =
        matchers.joinToString(", and ")
}

data class ChildMatcher(val relation: SyntaxRelation, val matcher: SyntaxNodeMatcher) : SyntaxNodeMatcher {
    override fun match(node: SyntaxNode): Boolean =
        node.children
            .firstOrNull { it.first == relation }
            ?.let { matcher.match(it.second) } == true

    override fun toString(): String =
        "has a child $relation (which $matcher)"
}

data class WordSpeechPartMatcher(val speechPart: SpeechPart) : SyntaxNodeMatcher {
    override fun match(node: SyntaxNode): Boolean =
        node.word
            .semanticsCore
            .speechPart
            .type == speechPart

    override fun toString(): String =
        "is $speechPart"
}

data class WordTagMatcher(val tag: SemanticsTag) : SyntaxNodeMatcher {
    constructor(tagName: String) : this(SemanticsTag(tagName))

    override fun match(node: SyntaxNode): Boolean =
        node.word
            .semanticsCore
            .tags
            .contains(tag)

    override fun toString(): String =
        "is ${tag.name}"
}


operator fun SyntaxNodeMatcher.plus(other: SyntaxNodeMatcher) = MulMatcher(
    if (this is MulMatcher && other is MulMatcher)
        matchers + other.matchers
    else if (this is MulMatcher)
        matchers + other
    else if (other is MulMatcher)
        listOf(this) + other.matchers
    else
        listOf(this, other)
)

infix fun SyntaxRelation.matches(matcher: SyntaxNodeMatcher) =
    ChildMatcher(this, matcher)

fun of(speechPart: SpeechPart) = WordSpeechPartMatcher(speechPart)

fun has(tagName: String) = WordTagMatcher(tagName)
