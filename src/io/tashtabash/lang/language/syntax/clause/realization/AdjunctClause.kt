package io.tashtabash.lang.language.syntax.clause.realization

import io.tashtabash.lang.language.Language
import io.tashtabash.lang.language.category.CaseValue
import io.tashtabash.lang.language.syntax.SyntaxRelation
import io.tashtabash.lang.language.syntax.clause.translation.SentenceNode
import kotlin.random.Random


interface AdjunctClause : SyntaxClause {
    val relation: SyntaxRelation
}

class CaseAdjunctClause(
    val nominal: NominalClause,
    val case: CaseValue,
    override val relation: SyntaxRelation
) : AdjunctClause {
    override fun toNode(language: Language, random: Random): SentenceNode {
        val speechPart = nominal.nominal.semanticsCore.speechPart
        val node = nominal.toNode(language, random)

        node.categoryValues.removeIf { it is CaseValue }
        node.categoryValues += language.changeParadigm.syntaxLogic.resolveNonCoreCase(case, speechPart)

        return node
    }
}
