package io.tashtabash.lang.language.syntax.transformer

import io.tashtabash.lang.language.syntax.SubstitutingOrder
import io.tashtabash.lang.language.syntax.SyntaxException
import io.tashtabash.lang.language.syntax.SyntaxLogic
import io.tashtabash.lang.language.syntax.SyntaxRelation
import io.tashtabash.lang.language.syntax.arranger.RelationArranger
import io.tashtabash.lang.language.syntax.clause.syntax.SyntaxNode


interface Transformer {
    fun apply(node: SyntaxNode, syntaxLogic: SyntaxLogic)
}

data class MulTransformer(val transformers: List<Transformer>) : Transformer {
    override fun apply(node: SyntaxNode, syntaxLogic: SyntaxLogic) =
        transformers.forEach { it.apply(node, syntaxLogic) }

    override fun toString(): String =
        transformers.joinToString(", and ")
}

data class ChildTransformer(private val relation: SyntaxRelation, private val transformer: Transformer): Transformer {
    override fun apply(node: SyntaxNode, syntaxLogic: SyntaxLogic) {
        node.children
            .firstOrNull { it.first == relation }
            ?.let { transformer.apply(it.second, syntaxLogic) }
    }

    override fun toString(): String =
        "the child $relation $transformer"
}

data class RemoveCategoryTransformer(private val categoryName: String): Transformer {
    override fun apply(node: SyntaxNode, syntaxLogic: SyntaxLogic) {
        node.categoryValues.removeIf { it.parentClassName == categoryName }
    }

    override fun toString(): String =
        "remove category values for $categoryName"
}

data class AddCategoryTransformer(private val relation: SyntaxRelation): Transformer {
    override fun apply(node: SyntaxNode, syntaxLogic: SyntaxLogic) {
        node.categoryValues += syntaxLogic.resolveSyntaxRelationToCase(relation, node.word.semanticsCore.speechPart)
    }

    override fun toString(): String =
        "remove add category values for $relation"
}

data class RemapOrderTransformer(private val substitutions: Map<SyntaxRelation, SyntaxRelation>): Transformer {
    override fun apply(node: SyntaxNode, syntaxLogic: SyntaxLogic) {
        val arranger = node.arranger
        if (arranger !is RelationArranger)
            throw SyntaxException("RelationArranger was expected")

        node.arranger = RelationArranger(
            SubstitutingOrder(arranger.relationOrder, substitutions)
        )
    }

    override fun toString(): String =
        substitutions.entries.joinToString { (o, n) -> "$n receives the place of $o" }
}

data object DropTransformer: Transformer {
    override fun apply(node: SyntaxNode, syntaxLogic: SyntaxLogic) {
        node.isDropped = true
    }

    override fun toString(): String =
        "is dropped"
}


operator fun Transformer.plus(other: Transformer) = MulTransformer(
    if (this is MulTransformer && other is MulTransformer)
        transformers + other.transformers
    else if (this is MulTransformer)
        transformers + other
    else if (other is MulTransformer)
        listOf(this) + other.transformers
    else
        listOf(this, other)
)
