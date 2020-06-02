package shmp.language.syntax

import shmp.language.CategoryValue

data class Sentence(val node: SentenceNode)

data class SentenceNode(
    val clause: Clause,
    val categories: List<CategoryValue>,
    val relation: Map<SyntaxRelation, SentenceNode>
)