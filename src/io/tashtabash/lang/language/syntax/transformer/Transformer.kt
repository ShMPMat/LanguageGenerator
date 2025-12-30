package io.tashtabash.lang.language.syntax.transformer

import io.tashtabash.lang.language.syntax.*
import io.tashtabash.lang.language.syntax.arranger.RelationArranger
import io.tashtabash.lang.language.syntax.clause.syntax.SyntaxNode
import io.tashtabash.lang.language.syntax.clause.syntax.SyntaxNodeTag
import io.tashtabash.random.withProb


interface Transformer {
    fun apply(node: SyntaxNode, syntaxLogic: SyntaxLogic)
}

data class MulTransformer(val transformers: List<Transformer>) : Transformer {
    override fun apply(node: SyntaxNode, syntaxLogic: SyntaxLogic) =
        transformers.forEach { it.apply(node, syntaxLogic) }

    override fun toString(): String =
        transformers.joinToString(", and ")
}

data class RelationTransformer(private val relation: SyntaxRelation, private val transformer: Transformer): Transformer {
    override fun apply(node: SyntaxNode, syntaxLogic: SyntaxLogic) {
        node.relations[relation]
            ?.let { transformer.apply(it, syntaxLogic) }
    }

    override fun toString(): String =
        "for $relation $transformer"
}

data class RemoveCategoryTransformer(private val categoryNames: List<String>): Transformer {
    constructor(vararg categoryNames: String) : this(categoryNames.toList())

    override fun apply(node: SyntaxNode, syntaxLogic: SyntaxLogic) {
        node.categoryValues.removeIf { it.parentClassName in categoryNames }
    }

    override fun toString(): String =
        "remove category values for ${categoryNames.joinToString()}"
}

data class AddCategoryTransformer(private val relation: SyntaxRelation): Transformer {
    override fun apply(node: SyntaxNode, syntaxLogic: SyntaxLogic) {
        node.categoryValues += syntaxLogic.resolveSyntaxRelationToCase(relation, node.word.semanticsCore.speechPart)
    }

    override fun toString(): String =
        "add category values for $relation"
}

data class AddTagTransformer(private val tag: SyntaxNodeTag): Transformer {
    override fun apply(node: SyntaxNode, syntaxLogic: SyntaxLogic) {
        node.tags += tag
    }

    override fun toString(): String =
        "add tag $tag"
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

// Order the node to be the first in the parent's Arranger
data class PutFirstTransformer(private val parentRelation: SyntaxRelation): Transformer {
    override fun apply(node: SyntaxNode, syntaxLogic: SyntaxLogic) {
        val parent = node.relations[parentRelation]
            ?: return
        val arranger = parent.arranger
        if (arranger !is RelationArranger)
            throw SyntaxException("RelationArranger was expected")
        val childRelation = parent.children.first { it.second == node }
            .first

        parent.arranger = RelationArranger(
           RandomOrder(
               arranger.relationOrder
                   .references
                   .map { (listOf(childRelation) + it.value).withProb(it.probability) }
           )
        )
    }

    override fun toString(): String =
        "put it first for parent $parentRelation"
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

fun add(tag: SyntaxNodeTag) = AddTagTransformer(tag)
fun add(relation: SyntaxRelation) = AddCategoryTransformer(relation)
fun remove(vararg categoryNames: String) = RemoveCategoryTransformer(categoryNames.toList())

fun transform(relation: SyntaxRelation, expr: () -> Transformer) =
    RelationTransformer(relation, expr())
