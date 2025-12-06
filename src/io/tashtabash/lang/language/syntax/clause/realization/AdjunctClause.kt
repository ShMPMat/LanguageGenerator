package io.tashtabash.lang.language.syntax.clause.realization

import io.tashtabash.lang.language.Language
import io.tashtabash.lang.language.category.CaseValue
import io.tashtabash.lang.language.syntax.SyntaxRelation
import io.tashtabash.lang.language.syntax.clause.syntax.SyntaxNode
import kotlin.random.Random


interface AdjunctClause : SyntaxClause {
    val relation: SyntaxRelation
}

class CaseAdjunctClause(
    val nominal: NominalClause,
    override val relation: SyntaxRelation
) : AdjunctClause {
    override fun toNode(language: Language, random: Random): SyntaxNode {
        val speechPart = nominal.nominal.semanticsCore.speechPart
        val node = nominal.toNode(language, random)

        node.categoryValues.removeIf { it is CaseValue }
        node.categoryValues += language.changeParadigm.syntaxLogic.resolveSyntaxRelationToCase(relation, speechPart)

        return node
    }
}
