package io.tashtabash.lang.language.syntax.clause.realization

import io.tashtabash.lang.language.Language
import io.tashtabash.lang.language.syntax.SyntaxRelation
import io.tashtabash.lang.language.syntax.clause.syntax.SyntaxNode
import io.tashtabash.lang.language.syntax.transformer.AddCategoryTransformer
import kotlin.random.Random


interface AdjunctClause : SyntaxClause {
    val relation: SyntaxRelation
}

class CaseAdjunctClause(
    val nominal: NominalClause,
    override val relation: SyntaxRelation
) : AdjunctClause {
    override fun toNode(language: Language, random: Random): SyntaxNode {
        val node = nominal.toNode(language, random)
        AddCategoryTransformer(relation).apply(node, language.changeParadigm.syntaxLogic)

        return node
    }
}
