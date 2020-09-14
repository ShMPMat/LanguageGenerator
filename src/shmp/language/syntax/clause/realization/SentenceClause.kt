package shmp.language.syntax.clause.realization

import shmp.language.Language
import shmp.language.syntax.ChangeParadigm
import shmp.language.syntax.clause.translation.SentenceClauseConstructor
import shmp.language.syntax.clause.translation.SentenceNode
import shmp.language.syntax.clause.translation.SentenceType
import shmp.language.syntax.SyntaxRelation
import shmp.language.syntax.orderer.RelationOrderer
import kotlin.random.Random


interface SentenceClause : UnfoldableClause {
    val type: SentenceType
}


class TransitiveVerbSentenceClause(
    private val verbClause: TransitiveVerbClause,
    override val type: SentenceType
) : SentenceClause {
    override fun toNode(changeParadigm: ChangeParadigm, random: Random): SentenceNode =
        verbClause.toNode(changeParadigm, random)
            .copy(orderer = RelationOrderer(changeParadigm.wordOrder.sovOrder.getValue(type), random))

    override fun unfold(language: Language, random: Random) =
        SentenceClauseConstructor(language.changeParadigm)
            .applyNode(toNode(language.changeParadigm, random), SyntaxRelation.Verb).second
}
