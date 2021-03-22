package shmp.lang.language.syntax.clause.realization

import shmp.lang.language.Language
import shmp.lang.language.syntax.ChangeParadigm
import shmp.lang.language.syntax.CopulaWordOrder
import shmp.lang.language.syntax.clause.translation.SentenceClauseTranslator
import shmp.lang.language.syntax.clause.translation.SentenceNode
import shmp.lang.language.syntax.clause.translation.VerbSentenceType
import shmp.lang.language.syntax.SyntaxRelation
import shmp.lang.language.syntax.arranger.PassingArranger
import shmp.lang.language.syntax.clause.translation.CopulaSentenceType
import shmp.lang.language.syntax.arranger.RelationArranger
import shmp.lang.language.syntax.features.QuestionMarker
import shmp.lang.language.syntax.features.WordSyntaxRole
import kotlin.random.Random


interface SentenceClause : UnfoldableClause


class TransitiveVerbSentenceClause(
    private val verbClause: TransitiveVerbClause,
    val type: VerbSentenceType
) : SentenceClause {
    override fun toNode(language: Language, random: Random): SentenceNode =
        verbClause.toNode(language, random).apply {
            if (language.changeParadigm.syntaxParadigm.questionMarkerPresence.questionMarker != null)
                setRelationChild(
                    SyntaxRelation.QuestionMarker,
                    language.lexis.getQuestionMarkerWord(QuestionMarker)
                        .copy(syntaxRole = WordSyntaxRole.QuestionMarker)
                        .wordToNode(language.changeParadigm, PassingArranger, SyntaxRelation.QuestionMarker)
                )
            arranger = RelationArranger(language.changeParadigm.wordOrder.sovOrder.getValue(type))
        }

    override fun unfold(language: Language, random: Random) =
        SentenceClauseTranslator(language.changeParadigm)
            .applyNode(toNode(language, random), SyntaxRelation.Verb, random).second
}

class CopulaSentenceClause(
    private val copulaClause: CopulaClause,
    val type: CopulaSentenceType
) : SentenceClause {
    override fun toNode(language: Language, random: Random): SentenceNode =
        copulaClause.toNode(language, random).apply {
            if (language.changeParadigm.syntaxParadigm.questionMarkerPresence.questionMarker != null)
                setRelationChild(
                    SyntaxRelation.QuestionMarker,
                    language.lexis.getQuestionMarkerWord(QuestionMarker)
                        .copy(syntaxRole = WordSyntaxRole.QuestionMarker)
                        .wordToNode(language.changeParadigm, PassingArranger, SyntaxRelation.QuestionMarker)
                )
            arranger = language.changeParadigm.wordOrder.copulaOrder.getValue(CopulaWordOrder(type, copulaClause.copulaType))
        }

    override fun unfold(language: Language, random: Random) =
        SentenceClauseTranslator(language.changeParadigm)
            .applyNode(toNode(language, random), copulaClause.topType, random).second
}
