package io.tashtabash.lang.language.syntax.transformer

import io.tashtabash.lang.language.lexis.SemanticsTag
import io.tashtabash.lang.language.lexis.SpeechPart
import io.tashtabash.lang.language.lexis.TypedSpeechPart
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

// A child exists and matches the predicate if it's specified
data class ChildMatcher(val relation: SyntaxRelation, val matcher: SyntaxNodeMatcher? = null) : SyntaxNodeMatcher {
    override fun match(node: SyntaxNode): Boolean =
        node.children
            .firstOrNull { it.first == relation }
            ?.let { matcher?.match(it.second) ?: true } == true

    override fun toString(): String =
        if (matcher != null)
            "has a child $relation (which $matcher)"
        else
            "has a child $relation"
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

data class TypedWordSpeechPartMatcher(val speechPart: TypedSpeechPart) : SyntaxNodeMatcher {
    override fun match(node: SyntaxNode): Boolean =
        node.word
            .semanticsCore
            .speechPart == speechPart

    override fun toString(): String =
        "is $speechPart"
}

data class WordTagMatcher(val tag: SemanticsTag) : SyntaxNodeMatcher {
    override fun match(node: SyntaxNode): Boolean =
        node.word
            .semanticsCore
            .tags
            .contains(tag)

    override fun toString(): String =
        "is ${tag.name}"
}

data class CategoryMatcher(val categoryName: String) : SyntaxNodeMatcher {
    override fun match(node: SyntaxNode): Boolean =
        node.word
            .categoryValues
            .any { it.parent.category.outType == categoryName }

    override fun toString(): String =
        "has a category value for $categoryName"
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
fun of(speechPart: TypedSpeechPart) = TypedWordSpeechPartMatcher(speechPart)

fun has(tag: SemanticsTag) = WordTagMatcher(tag)
fun has(categoryName: String) = CategoryMatcher(categoryName)
fun has(relation: SyntaxRelation) = ChildMatcher(relation)
