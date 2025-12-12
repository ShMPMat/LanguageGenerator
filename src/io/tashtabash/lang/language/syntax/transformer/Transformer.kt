package io.tashtabash.lang.language.syntax.transformer

import io.tashtabash.lang.language.syntax.SubstitutingOrder
import io.tashtabash.lang.language.syntax.SyntaxException
import io.tashtabash.lang.language.syntax.SyntaxRelation
import io.tashtabash.lang.language.syntax.arranger.RelationArranger
import io.tashtabash.lang.language.syntax.clause.syntax.SyntaxNode


interface Transformer {
    fun apply(node: SyntaxNode)
}

data class RemapOrderTransformer(private val substitutions: Map<SyntaxRelation, SyntaxRelation>): Transformer {
    override fun apply(node: SyntaxNode) {
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


