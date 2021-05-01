package shmp.lang.language.syntax.clause.realization

import shmp.lang.language.Language
import shmp.lang.language.syntax.CopulaWordOrder
import shmp.lang.language.syntax.SubstitutingOrder
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


abstract class SentenceClause(private val topNodeRelation: SyntaxRelation) : UnfoldableClause {
    final override fun unfold(language: Language, random: Random) =
        SentenceClauseTranslator(language.changeParadigm)
            .applyNode(toNode(language, random), topNodeRelation, random).second
}


class TransitiveVerbSentenceClause(
    private val verbClause: TransitiveVerbClause,
    val type: VerbSentenceType
) : SentenceClause(SyntaxRelation.Verb) {
    override fun toNode(language: Language, random: Random): SentenceNode =
        verbClause.toNode(language, random).apply {
            if (type == VerbSentenceType.QuestionVerbClause)
                if (language.changeParadigm.syntaxParadigm.questionMarkerPresence.questionMarker != null)
                    setRelationChild(
                        SyntaxRelation.QuestionMarker,
                        language.lexis.getQuestionMarkerWord(QuestionMarker)
                            .copy(syntaxRole = WordSyntaxRole.QuestionMarker)
                            .wordToNode(PassingArranger, SyntaxRelation.QuestionMarker)
                    )
            arranger = RelationArranger(language.changeParadigm.wordOrder.sovOrder.getValue(type))
        }
}

class IntransitiveVerbSentenceClause(
    private val verbClause: IntransitiveVerbClause,
    val type: VerbSentenceType
) : SentenceClause(SyntaxRelation.Verb) {
    override fun toNode(language: Language, random: Random): SentenceNode =
        verbClause.toNode(language, random).apply {
            if (type == VerbSentenceType.QuestionVerbClause)
                if (language.changeParadigm.syntaxParadigm.questionMarkerPresence.questionMarker != null)
                    setRelationChild(
                        SyntaxRelation.QuestionMarker,
                        language.lexis.getQuestionMarkerWord(QuestionMarker)
                            .copy(syntaxRole = WordSyntaxRole.QuestionMarker)
                            .wordToNode(PassingArranger, SyntaxRelation.QuestionMarker)
                    )
            arranger = RelationArranger(SubstitutingOrder(
                language.changeParadigm.wordOrder.sovOrder.getValue(type)
            ) { lst ->
                lst.map { r ->
                    if (r == SyntaxRelation.Agent)
                        SyntaxRelation.Argument
                    else r
                }
            })
        }
}

class CopulaSentenceClause(
    private val copulaClause: CopulaClause,
    val type: CopulaSentenceType
) : SentenceClause(copulaClause.topType) {
    override fun toNode(language: Language, random: Random): SentenceNode =
        copulaClause.toNode(language, random).apply {
            if (type == CopulaSentenceType.QuestionCopulaClause)
                if (language.changeParadigm.syntaxParadigm.questionMarkerPresence.questionMarker != null)
                    setRelationChild(
                        SyntaxRelation.QuestionMarker,
                        language.lexis.getQuestionMarkerWord(QuestionMarker)
                            .copy(syntaxRole = WordSyntaxRole.QuestionMarker)
                            .wordToNode(PassingArranger, SyntaxRelation.QuestionMarker)
                    )
            arranger =
                language.changeParadigm.wordOrder.copulaOrder.getValue(CopulaWordOrder(type, copulaClause.copulaType))
        }
}
