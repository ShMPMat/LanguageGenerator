package io.tashtabash.lang.language.syntax.clause.realization

import io.tashtabash.lang.language.Language
import io.tashtabash.lang.language.category.NegationValue
import io.tashtabash.lang.language.syntax.SubstitutingOrder
import io.tashtabash.lang.language.syntax.clause.translation.SentenceClauseTranslator
import io.tashtabash.lang.language.syntax.clause.translation.SentenceNode
import io.tashtabash.lang.language.syntax.clause.translation.VerbSentenceType
import io.tashtabash.lang.language.syntax.SyntaxRelation
import io.tashtabash.lang.language.syntax.clause.translation.CopulaSentenceType
import io.tashtabash.lang.language.syntax.arranger.RelationArranger
import io.tashtabash.lang.language.syntax.features.QuestionMarker
import io.tashtabash.lang.language.syntax.features.WordSyntaxRole
import kotlin.random.Random


abstract class SentenceClause : UnfoldableClause {
    final override fun unfold(language: Language, random: Random) =
        SentenceClauseTranslator(language.changeParadigm)
            .applyNode(toNode(language, random), random)
}


fun SentenceNode.addQuestionMarker(language: Language) {
    if (language.changeParadigm.syntaxParadigm.questionMarkerPresence.questionMarker != null)
        setRelationChild(
            SyntaxRelation.QuestionMarker,
            language.lexis.getQuestionMarkerWord(QuestionMarker)
                .copy(syntaxRole = WordSyntaxRole.QuestionMarker)
                .toNode(SyntaxRelation.QuestionMarker)
        )
}


class TransitiveVerbSentenceClause(
    private val verbClause: TransitiveVerbClause,
    val type: VerbSentenceType
) : SentenceClause() {
    override fun toNode(language: Language, random: Random): SentenceNode =
        verbClause.toNode(language, random).apply {
            if (type == VerbSentenceType.QuestionVerbClause)
                addQuestionMarker(language)

            if (type == VerbSentenceType.NegatedVerbClause)
                categoryValues += NegationValue.Negative

            arranger = RelationArranger(language.changeParadigm.wordOrder.sovOrder.getValue(type))
        }
}

class IntransitiveVerbSentenceClause(
    private val verbClause: IntransitiveVerbClause,
    val type: VerbSentenceType
) : SentenceClause() {
    override fun toNode(language: Language, random: Random): SentenceNode =
        verbClause.toNode(language, random).apply {
            if (type == VerbSentenceType.QuestionVerbClause)
                addQuestionMarker(language)

            if (type == VerbSentenceType.NegatedVerbClause)
                categoryValues += NegationValue.Negative

            arranger = RelationArranger(SubstitutingOrder(
                language.changeParadigm.wordOrder.sovOrder.getValue(type),
                mapOf(SyntaxRelation.Agent to SyntaxRelation.Argument)
            ))
        }
}

class CopulaSentenceClause(private val copulaClause: CopulaClause, val type: CopulaSentenceType) : SentenceClause() {
    override fun toNode(language: Language, random: Random): SentenceNode =
        copulaClause.toNode(language, random).apply {
            if (type == CopulaSentenceType.QuestionCopulaClause)
                addQuestionMarker(language)

            if (type == CopulaSentenceType.NegatedCopulaClause)
                categoryValues += NegationValue.Negative

            arranger = language.changeParadigm
                .wordOrder
                .copulaOrder
                .getValue(copulaClause.copulaType)
                .getValue(type)
        }
}
