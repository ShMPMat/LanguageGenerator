package shmp.lang.language.syntax.clause.realization

import shmp.lang.language.Language
import shmp.lang.language.category.CaseValue
import shmp.lang.language.syntax.SyntaxRelation
import shmp.lang.language.syntax.clause.translation.SentenceNode
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
        val node = nominal.toNode(language, random)

        node.categoryValues.removeIf { it is CaseValue }
        node.categoryValues.addAll(
            language.changeParadigm.syntaxLogic.resolveNonCoreCase(case, nominal.nominal.semanticsCore.speechPart)
        )

        return node
    }
}

