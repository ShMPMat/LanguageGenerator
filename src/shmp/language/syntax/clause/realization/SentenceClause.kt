package shmp.language.syntax.clause.realization

import shmp.language.Language
import shmp.language.syntax.ChangeParadigm
import shmp.language.syntax.CopulaWordOrder
import shmp.language.syntax.clause.translation.SentenceClauseTranslator
import shmp.language.syntax.clause.translation.SentenceNode
import shmp.language.syntax.clause.translation.VerbSentenceType
import shmp.language.syntax.SyntaxRelation
import shmp.language.syntax.clause.translation.CopulaSentenceType
import shmp.language.syntax.arranger.RelationArranger
import kotlin.random.Random


interface SentenceClause : UnfoldableClause


class TransitiveVerbSentenceClause(
    private val verbClause: TransitiveVerbClause,
    val type: VerbSentenceType
) : SentenceClause {
    override fun toNode(changeParadigm: ChangeParadigm, random: Random): SentenceNode =
        verbClause.toNode(changeParadigm, random)
            .copy(arranger = RelationArranger(changeParadigm.wordOrder.sovOrder.getValue(type)))

    override fun unfold(language: Language, random: Random) =
        SentenceClauseTranslator(language.changeParadigm)
            .applyNode(toNode(language.changeParadigm, random), SyntaxRelation.Verb, random).second
}

class CopulaSentenceClause(
    private val copulaClause: CopulaClause,
    val type: CopulaSentenceType
) : SentenceClause {
    override fun toNode(changeParadigm: ChangeParadigm, random: Random): SentenceNode =
        copulaClause.toNode(changeParadigm, random)
            .copy(arranger = changeParadigm.wordOrder.copulaOrder.getValue(CopulaWordOrder(type, copulaClause.copulaType)))

    override fun unfold(language: Language, random: Random) =
        SentenceClauseTranslator(language.changeParadigm)
            .applyNode(toNode(language.changeParadigm, random), copulaClause.topType, random).second
}
